package br.com.PhG22.sglib.view
// No pacote: view
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.controller.BookController

class WriteReviewActivity : AppCompatActivity() {

    private var bookId: String? = null
    private var bookTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)

        // 1. Pegar dados do livro vindos da BookDetailActivity
        bookId = intent.getStringExtra("BOOK_ID")
        bookTitle = intent.getStringExtra("BOOK_TITLE")

        if (bookId == null) {
            Toast.makeText(this, "Erro: Livro não identificado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Mapear as Views
        val tvTitle = findViewById<TextView>(R.id.tvReviewTitle)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etComment = findViewById<EditText>(R.id.etReviewComment)
        val btnPublish = findViewById<Button>(R.id.btnPublishReview)

        tvTitle.text = "Write a Review for $bookTitle"

        // 3. Configurar clique do botão
        btnPublish.setOnClickListener {
            val rating = ratingBar.rating
            val comment = etComment.text.toString().trim()

            if (rating == 0f) {
                Toast.makeText(this, "Por favor, selecione uma nota.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (comment.isEmpty()) {
                Toast.makeText(this, "Por favor, escreva um comentário.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPublish.isEnabled = false // Desabilita botão

            // 4. Chamar o Controller
            BookController.submitReview(bookId!!, rating, comment,
                onSuccess = {
                    Toast.makeText(this, "Resenha enviada para moderação.", Toast.LENGTH_LONG).show()
                    finish() // Fecha a activity e volta para os detalhes
                },
                onError = { errorMsg ->
                    Toast.makeText(this, "Erro: $errorMsg", Toast.LENGTH_LONG).show()
                    btnPublish.isEnabled = true // Reabilita
                }
            )
        }
    }
}