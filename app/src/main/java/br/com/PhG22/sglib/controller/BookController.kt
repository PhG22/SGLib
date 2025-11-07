package br.com.PhG22.sglib.controller

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import br.com.PhG22.sglib.R
import br.com.PhG22.sglib.model.Emprestimo
import br.com.PhG22.sglib.model.Livro
import br.com.PhG22.sglib.model.Resenha
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.ByteArrayOutputStream

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
     * Tenta solicitar um empréstimo.
     * AGORA VERIFICA SE O UTILIZADOR TEM MULTAS PENDENTES.
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

        // --- INÍCIO DA NOVA VERIFICAÇÃO (UC6) ---
        // 1. Verificar se o utilizador tem multas pendentes
        FinesController.getMyUnpaidFines(
            onSuccess = { finesList ->
                if (finesList.isNotEmpty()) {
                    // 2. BLOQUEIA o empréstimo se houver multas
                    onError("Não é possível solicitar empréstimos. Você possui multas pendentes.")
                    return@getMyUnpaidFines
                }

                // 3. Nenhuma multa encontrada. Procede para a verificação de estoque.
                if (book.quantidade <= 0) {
                    onError("Livro indisponível para empréstimo.")
                    return@getMyUnpaidFines
                }

                // 4. Todas as verificações passaram. Cria o empréstimo.
                val newLoan = Emprestimo(
                    bookId = book.id,
                    bookTitle = book.titulo,
                    bookImageUrl = book.imageUrl,
                    userId = userId,
                    status = "pending"
                )

                loansCollection.add(newLoan)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Erro ao solicitar empréstimo.")
                    }
            },
            onError = { errorMsg ->
                // Falha ao verificar multas, bloqueia por segurança
                onError("Erro ao verificar seus débitos: $errorMsg")
            }
        )
        // --- FIM DA NOVA VERIFICAÇÃO ---
    }

    /**
     * Adiciona ou atualiza um livro (lógica Base64).
     */
    fun addOrUpdateBook(
        livro: Livro,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        saveBookToFirestore(livro, onSuccess, onError)
    }

    private fun saveBookToFirestore(
        livro: Livro,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (livro.id.isEmpty()) {
            booksCollection.add(livro)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Erro ao salvar livro.") }
        } else {
            booksCollection.document(livro.id).set(livro)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Erro ao atualizar livro.") }
        }
    }

    /**
     * Exclui um livro do Firestore (lógica Base64).
     */
    fun deleteBook(
        livro: Livro,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        booksCollection.document(livro.id).delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError("Erro ao excluir livro: ${e.message}")
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

        usersCollection.document(userId).get()
            .addOnSuccessListener { userDoc ->
                val userName = userDoc.getString("nome") ?: "Usuário Anônimo"

                val newReview = Resenha(
                    bookId = bookId,
                    userId = userId,
                    userName = userName,
                    rating = rating,
                    comment = comment,
                    isApproved = false // Campo 'approved' como no AdminController
                )

                reviewsCollection.add(newReview)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Erro ao enviar resenha.") }
            }
            .addOnFailureListener { e ->
                onError("Erro ao buscar dados do usuário: ${e.message}")
            }
    }

    fun getApprovedReviews(
        bookId: String,
        onSuccess: (List<Resenha>) -> Unit,
        onError: (String) -> Unit
    ) {
        reviewsCollection
            .whereEqualTo("bookId", bookId)
            .whereEqualTo("approved", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
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