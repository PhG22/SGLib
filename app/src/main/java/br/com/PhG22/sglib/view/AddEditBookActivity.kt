package br.com.PhG22.sglib.view

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.BookController
import br.com.PhG22.sglib.model.Livro
import com.bumptech.glide.Glide

class AddEditBookActivity : AppCompatActivity() {

    private lateinit var tvTitleHeader: TextView
    private lateinit var ivCover: ImageView
    private lateinit var btnUploadCover: Button
    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etPublisher: EditText
    private lateinit var etYear: EditText
    private lateinit var etQuantity: EditText
    private lateinit var btnSave: Button

    private var imageUri: Uri? = null
    private var editModeBookId: String? = null
    private var existingImageUrl: String? = null

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imageUri = uri
                Glide.with(this).load(imageUri).into(ivCover)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_book)

        tvTitleHeader = findViewById(R.id.tvTitleHeader)
        ivCover = findViewById(R.id.ivBookCoverUpload)
        btnUploadCover = findViewById(R.id.btnUploadCover)
        etTitle = findViewById(R.id.etBookTitle)
        etAuthor = findViewById(R.id.etBookAuthor)
        etPublisher = findViewById(R.id.etBookPublisher)
        etYear = findViewById(R.id.etBookYear)
        etQuantity = findViewById(R.id.etBookQuantity)
        btnSave = findViewById(R.id.btnSaveBook)

        editModeBookId = intent.getStringExtra("EXTRA_BOOK_ID")

        if (editModeBookId != null) {
            btnSave.text = "Atualizar Livro"
            btnUploadCover.text = "Trocar Imagem da Capa"
            loadBookDataForEdit(editModeBookId!!)
        } else {
            btnSave.text = "Salvar Novo Livro"
        }

        btnUploadCover.setOnClickListener { launchPhotoPicker() }
        btnSave.setOnClickListener { saveBook() }
    }

    private fun loadBookDataForEdit(bookId: String) {
        BookController.getBookById(bookId,
            onSuccess = { livro ->
                etTitle.setText(livro.titulo)
                etAuthor.setText(livro.autor)
                etPublisher.setText(livro.editora)
                etYear.setText(livro.anoPublicacao.toString())
                etQuantity.setText(livro.quantidade.toString())

                existingImageUrl = livro.imageUrl

                Glide.with(this).load(livro.imageUrl).into(ivCover)
            },
            onError = { errorMsg ->
                Toast.makeText(this, "Erro ao carregar dados: $errorMsg", Toast.LENGTH_LONG).show()
                finish()
            }
        )
    }

    private fun launchPhotoPicker() {
        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun saveBook() {
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()
        val publisher = etPublisher.text.toString().trim()
        val yearStr = etYear.text.toString().trim()
        val quantityStr = etQuantity.text.toString().trim()

        if (title.isEmpty() || author.isEmpty() || yearStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_LONG).show()
            return
        }

        if (editModeBookId == null && imageUri == null) {
            Toast.makeText(this, "Por favor, selecione uma imagem de capa.", Toast.LENGTH_LONG).show()
            return
        }

        val year = yearStr.toIntOrNull()
        val quantity = quantityStr.toIntOrNull()

        if (year == null || quantity == null) {
            Toast.makeText(this, "Ano e Quantidade devem ser números válidos.", Toast.LENGTH_LONG).show()
            return
        }

        setLoading(true)

        val imageUrlToSave = if (imageUri == null) existingImageUrl ?: "" else ""

        val livroParaSalvar = Livro(
            id = editModeBookId ?: "",
            titulo = title,
            autor = author,
            editora = publisher,
            anoPublicacao = year,
            quantidade = quantity,
            imageUrl = imageUrlToSave
        )

        BookController.addOrUpdateBook(livroParaSalvar, imageUri,
            onSuccess = {
                setLoading(false)
                Toast.makeText(this, "Livro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { errorMessage ->
                setLoading(false)
                Toast.makeText(this, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun setLoading(isLoading: Boolean) {
        btnSave.isEnabled = !isLoading
    }
}