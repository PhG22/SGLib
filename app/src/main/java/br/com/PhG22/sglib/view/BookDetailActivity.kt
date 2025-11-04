package br.com.PhG22.sglib.view
// No pacote: view
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.BookController
import br.com.PhG22.sglib.model.Livro
import com.bumptech.glide.Glide
import android.content.Intent // Importar
import androidx.recyclerview.widget.LinearLayoutManager // Importar
import androidx.recyclerview.widget.RecyclerView // Importar
import br.com.PhG22.sglib.model.Resenha // Importar
import br.com.PhG22.sglib.view.adapters.ReviewAdapter // Importar
import android.util.Log

class BookDetailActivity : AppCompatActivity() {

    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvPublisher: TextView
    private lateinit var tvAvailability: TextView
    private lateinit var btnRequestLoan: Button

    private lateinit var btnWriteReview: Button // Nova View

    private lateinit var rvReviews: RecyclerView // Nova View
    private lateinit var reviewAdapter: ReviewAdapter // Novo Adapte
    private var currentBook: Livro? = null
    private var bookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        // 1. Pegar o ID do livro enviado pela UserMainActivity
        bookId = intent.getStringExtra("BOOK_ID")
        if (bookId == null) {
            Toast.makeText(this, "Erro: ID do livro não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Mapear as Views
        ivCover = findViewById(R.id.ivBookCoverDetail)
        tvTitle = findViewById(R.id.tvBookTitleDetail)
        tvAuthor = findViewById(R.id.tvBookAuthorDetail)
        tvPublisher = findViewById(R.id.tvBookPublisherDetail)
        tvAvailability = findViewById(R.id.tvBookAvailability)
        btnRequestLoan = findViewById(R.id.btnRequestLoan)
        btnWriteReview = findViewById(R.id.btnWriteReview) // Mapear botão
        rvReviews = findViewById(R.id.recyclerViewReviews) // Mapear RecyclerView

        // 3. Configurar o clique do botão
        btnRequestLoan.setOnClickListener {
            currentBook?.let { book ->
                requestLoan(book)
            }
        }

        btnWriteReview.setOnClickListener {
            // Inicia a WriteReviewActivity
            val intent = Intent(this, WriteReviewActivity::class.java)
            intent.putExtra("BOOK_ID", bookId)
            intent.putExtra("BOOK_TITLE", currentBook?.titulo)
            startActivity(intent)
        }

        // 4. Carregar os dados do livro
        loadBookData()

        // 5. Configurar RecyclerView de Resenhas
        setupReviewsRecyclerView()

        // 6. Carregar as resenhas (faremos isso no onResume)
    }

    override fun onResume() {
        super.onResume()
        // Carregamos no onResume para que a lista atualize
        // quando voltarmos da WriteReviewActivity (caso uma nova
        // resenha tenha sido aprovada magicamente)
        loadReviews()
    }

    private fun setupReviewsRecyclerView() {
        reviewAdapter = ReviewAdapter(emptyList())
        rvReviews.layoutManager = LinearLayoutManager(this)
        rvReviews.adapter = reviewAdapter
    }
    private fun loadBookData() {
        bookId?.let { id ->
            BookController.getBookById(id,
                onSuccess = { book ->
                    currentBook = book // Salva o livro atual
                    // 5. Popular a UI com os dados
                    tvTitle.text = book.titulo
                    tvAuthor.text = book.autor
                    tvPublisher.text = "Publicado por ${book.editora}, ${book.anoPublicacao}"

                    if (book.quantidade > 0) {
                        tvAvailability.text = "Available" // [cite: 328]
                        tvAvailability.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                        btnRequestLoan.isEnabled = true
                    } else {
                        tvAvailability.text = "Unavailable"
                        tvAvailability.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        btnRequestLoan.isEnabled = false
                    }

                    Glide.with(this).load(book.imageUrl).into(ivCover)
                },
                onError = { errorMsg ->
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun loadReviews() {
        bookId?.let { id ->

            // !! ADICIONE ESTA LINHA !!
            Log.d("BookDetailActivity", "Carregando resenhas para o bookId: $id")

            BookController.getApprovedReviews(id,
                onSuccess = { reviewList ->
                    reviewAdapter.updateData(reviewList)
                },
                onError = { errorMsg ->
                    Log.e("BookDetailActivity", "Erro ao buscar resenhas: $errorMsg")
                }
            )
        }
    }
    private fun requestLoan(book: Livro) {
        btnRequestLoan.isEnabled = false // Desabilita o botão

        BookController.requestLoan(book,
            onSuccess = {
                // Sucesso! Mostra o diálogo 
                showSuccessDialog()
            },
            onError = { errorMsg ->
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                btnRequestLoan.isEnabled = true // Reabilita em caso de erro
            }
        )
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Request Successful") // [cite: 185]
            .setMessage("Your loan request has been sent. Please wait for administrator authorization.") // [cite: 186]
            .setPositiveButton("OK") { dialog, _ -> // [cite: 188]
                dialog.dismiss()
                btnRequestLoan.text = "Request Sent (Pending)"
                // A lógica de "My Loans" (UC4) mostrará o status
            }
            .setCancelable(false)
            .show()
    }
}