package br.com.PhG22.sglib.view

// Imports necessários
    import android.Manifest
    import android.content.pm.PackageManager
    import android.graphics.Bitmap
    import android.graphics.ImageDecoder
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.provider.MediaStore
    import android.util.Base64
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.activity.result.PickVisualMediaRequest
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import androidx.core.content.FileProvider
    import br.com.PhG22.sglib.R
    import br.com.PhG22.sglib.controller.BookController
    import br.com.PhG22.sglib.model.Livro
    import com.bumptech.glide.Glide
    import java.io.ByteArrayOutputStream
    import java.io.File

class AddEditBookActivity : AppCompatActivity() {

    // Views
    private lateinit var tvTitleHeader: TextView
    private lateinit var ivCover: ImageView
    private lateinit var btnUploadCover: Button
    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etPublisher: EditText
    private lateinit var etYear: EditText
    private lateinit var etQuantity: EditText
    private lateinit var btnSave: Button

    // Variáveis de Estado
    private var imageUri: Uri? = null // Uri da Galeria OU da Câmera
    private var editModeBookId: String? = null
    private var existingImageUrl: String? = null // String Base64 antiga

    // --- NOVOS LAUNCHERS E VARIÁVEIS ---

    // 1. Launcher para permissão de Câmera
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permissão concedida, lança a câmera
                launchCamera()
            } else {
                Toast.makeText(this, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
            }
        }

    // 2. Launcher para o resultado da Galeria (Photo Picker)
    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                // Imagem da galeria selecionada
                imageUri = uri // Salva o Uri
                Glide.with(this).load(imageUri).into(ivCover)
            }
        }

    // 3. Launcher para o resultado da Câmera
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                // A foto foi tirada e salva no 'imageUri' que passámos
                // Não precisamos de fazer nada aqui, porque o 'imageUri' já foi atualizado
                // antes de lançar a câmera. Apenas carregamos a preview.
                Glide.with(this).load(imageUri).into(ivCover)
            }
        }
    // --- FIM DAS NOVAS VARIÁVEIS ---


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_book)

        // Mapear as Views
        tvTitleHeader = findViewById(R.id.tvTitleHeader)
        ivCover = findViewById(R.id.ivBookCoverUpload)
        btnUploadCover = findViewById(R.id.btnUploadCover)
        etTitle = findViewById(R.id.etBookTitle)
        etAuthor = findViewById(R.id.etBookAuthor)
        etPublisher = findViewById(R.id.etBookPublisher)
        etYear = findViewById(R.id.etBookYear)
        etQuantity = findViewById(R.id.etBookQuantity)
        btnSave = findViewById(R.id.btnSaveBook)

        // Verificar se estamos em Modo de Edição
        editModeBookId = intent.getStringExtra("EXTRA_BOOK_ID")
        if (editModeBookId != null) {
            tvTitleHeader.text = "Editar Livro"
            btnSave.text = "Atualizar Livro"
            btnUploadCover.text = "Trocar Imagem da Capa"
            loadBookDataForEdit(editModeBookId!!)
        } else {
            tvTitleHeader.text = "Adicionar Novo Livro"
            btnSave.text = "Salvar Novo Livro"
        }

        // --- LÓGICA DE CLIQUE ATUALIZADA ---
        btnUploadCover.setOnClickListener {
            // Mostra o diálogo de escolha
            showImageSourceDialog()
        }
        btnSave.setOnClickListener { saveBook() }
    }

    /**
     * Mostra um diálogo para o usuário escolher entre Câmera ou Galeria.
     */
    private fun showImageSourceDialog() {
        val options = arrayOf("Tirar Foto", "Escolher da Galeria")
        AlertDialog.Builder(this)
            .setTitle("Selecionar Imagem")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch() // Tirar Foto
                    1 -> launchGalleryPicker() // Escolher da Galeria
                }
            }
            .show()
    }

    /**
     * Inicia o seletor de fotos da galeria (o que já tínhamos).
     */
    private fun launchGalleryPicker() {
        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    /**
     * Verifica a permissão da câmera. Se concedida, lança a câmera.
     * Se não, pede a permissão.
     */
    private fun checkCameraPermissionAndLaunch() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> {
                // Permissão já concedida
                launchCamera()
            }
            else -> {
                // Pede a permissão
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Cria um ficheiro temporário e lança a app da câmera.
     */
    private fun launchCamera() {
        // Cria um ficheiro temporário na cache
        val photoFile = File(externalCacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")

        // Gera um Uri seguro para esse ficheiro usando o FileProvider
        imageUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            photoFile
        )

        // Lança a câmera, dizendo-lhe para salvar a foto neste Uri
        takePictureLauncher.launch(imageUri)
    }

    // ... (função loadBookDataForEdit - permanece igual) ...
    private fun loadBookDataForEdit(bookId: String) {
        BookController.getBookById(bookId,
            onSuccess = { livro ->
                etTitle.setText(livro.titulo)
                etAuthor.setText(livro.autor)
                etPublisher.setText(livro.editora)
                etYear.setText(livro.anoPublicacao.toString())
                etQuantity.setText(livro.quantidade.toString())
                existingImageUrl = livro.imageUrl
                Glide.with(this)
                    .load(livro.imageUrl)
                    .placeholder(R.drawable.ic_library_book)
                    .into(ivCover)
            },
            onError = { errorMsg ->
                Toast.makeText(this, "Erro ao carregar dados: $errorMsg", Toast.LENGTH_LONG).show()
                finish()
            }
        )
    }

    /**
     * Lógica de salvar (quase igual, mas agora usa 'imageUri' de ambas as fontes)
     */
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

        // Se estiver a adicionar (não a editar), a imagem é obrigatória
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

        try {
            val imageUrlToSave: String

            if (imageUri != null) {
                // Uma NOVA imagem foi selecionada (da câmera OU galeria)
                imageUrlToSave = convertUriToResizedBase64(imageUri!!)
            } else {
                // Nenhuma imagem nova, reutiliza a existente
                imageUrlToSave = existingImageUrl ?: ""
            }

            val livroParaSalvar = Livro(
                id = editModeBookId ?: "",
                titulo = title,
                autor = author,
                editora = publisher,
                anoPublicacao = year,
                quantidade = quantity,
                imageUrl = imageUrlToSave // String Base64
            )

            // Chama o Controller (que agora está mais simples)
            BookController.addOrUpdateBook(livroParaSalvar,
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
        } catch (e: Exception) {
            setLoading(false)
            Toast.makeText(this, "Erro ao processar imagem: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    /**
     * Converte um URI de imagem (da Câmera OU Galeria) em uma string Base64,
     * redimensionando a imagem para economizar espaço no Firestore.
     */
    private fun convertUriToResizedBase64(uri: Uri): String {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }

        val maxWidth = 400
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        if (originalWidth == 0) throw Exception("Largura da imagem é zero.")

        val newHeight = (originalHeight.toFloat() / originalWidth.toFloat()) * maxWidth
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight.toInt(), false)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()

        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        return "data:image/jpeg;base64,$base64String"
    }

    private fun setLoading(isLoading: Boolean) {
        // progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !isLoading
    }
}