package br.com.PhG22.sglib.controller

// No pacote: controller
import br.com.PhG22.sglib.model.Emprestimo
import br.com.PhG22.sglib.model.Livro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import br.com.PhG22.sglib.model.Resenha

object BookController {

    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")
    private val loansCollection = db.collection("loans")
    private val usersCollection = db.collection("users")
    private val reviewsCollection = db.collection("reviews")
    private val auth = FirebaseAuth.getInstance()

    fun getAllBooks(
        onSuccess: (List<Livro>) -> Unit,
        onError: (String) -> Unit
    ) {
        booksCollection.get()
            .addOnSuccessListener { snapshot ->
                val bookList = snapshot.toObjects(Livro::class.java)
                onSuccess(bookList)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Erro ao buscar livros.")
            }
    }

    /**
     * Busca um único livro pelo seu ID
     */
    fun getBookById(
        bookId: String,
        onSuccess: (Livro) -> Unit,
        onError: (String) -> Unit
    ) {
        booksCollection.document(bookId).get()
            .addOnSuccessListener { document ->
                val book = document.toObject(Livro::class.java)
                if (book != null) {
                    onSuccess(book)
                } else {
                    onError("Livro não encontrado.")
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Erro ao buscar livro.")
            }
    }

    /**
     * Inicia uma solicitação de empréstimo (UC4)
     */
    fun requestLoan(
        book: Livro,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Usuário não autenticado.")
            return
        }

        // TODO: Checar pré-condições (UC4)
        // 1. Checar se livro está disponível (quantidade > 0) [cite: 170]
        if (book.quantidade <= 0) {
            onError("Livro indisponível para empréstimo.")
            return
        }
        // 2. Checar ausência de débitos [cite: 171] (Implementaremos no UC6)

        // Cria o novo objeto de Empréstimo
        val newLoan = Emprestimo(
            bookId = book.id,
            bookTitle = book.titulo,
            bookImageUrl = book.imageUrl,
            userId = userId,
            status = "pending" // Solicitação pendente de aprovação do Admin [cite: 186]
        )

        // Salva na coleção 'loans'
        loansCollection.add(newLoan)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Erro ao solicitar empréstimo.")
            }
    }

    fun submitReview(
        bookId: String,
        rating: Float,
        comment: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Usuário não autenticado.")
            return
        }

        // 1. Precisamos buscar o nome do usuário para salvar na resenha
        usersCollection.document(userId).get()
            .addOnSuccessListener { userDoc ->
                val userName = userDoc.getString("nome") ?: "Usuário Anônimo"

                // 2. Cria o objeto Resenha
                val newReview = Resenha(
                    bookId = bookId,
                    userId = userId,
                    userName = userName,
                    rating = rating,
                    comment = comment,
                    isApproved = false // Requer aprovação do Admin
                )

                // 3. Salva na coleção 'reviews'
                reviewsCollection.add(newReview)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Erro ao enviar resenha.") }
            }
            .addOnFailureListener { e ->
                onError("Erro ao buscar dados do usuário: ${e.message}")
            }
    }

    /**
     * Busca resenhas aprovadas para um livro específico (UC7)
     */
    fun getApprovedReviews(
        bookId: String,
        onSuccess: (List<Resenha>) -> Unit,
        onError: (String) -> Unit
    ) {
        reviewsCollection
            .whereEqualTo("bookId", bookId) // Filtra pelo livro
            .whereEqualTo("isApproved", true) // Filtra apenas aprovadas
            .orderBy("timestamp", Query.Direction.DESCENDING) // Mais novas primeiro
            .get()
            .addOnSuccessListener { snapshot ->
                val reviewList = snapshot.toObjects(Resenha::class.java)
                onSuccess(reviewList)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Erro ao buscar resenhas.")
            }
    }
}