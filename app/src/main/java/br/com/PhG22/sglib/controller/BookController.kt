package br.com.PhG22.sglib.controller

// No pacote: controller
import br.com.PhG22.sglib.model.Emprestimo
import br.com.PhG22.sglib.model.Livro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import br.com.PhG22.sglib.model.Resenha
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import android.util.Log

object BookController {

    private val db = FirebaseFirestore.getInstance()

    private val storage = FirebaseStorage.getInstance()
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
            .whereEqualTo("approved", true) // Filtra apenas aprovadas
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

    /**
     * Adiciona ou atualiza um livro.
     * Primeiro, faz o upload da imagem (se houver uma nova) e depois salva no Firestore.
     */
    fun addOrUpdateBook(
        livro: Livro,
        imageUri: Uri?, // A Uri local da imagem selecionada
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Se uma nova imagem foi fornecida (imageUri != null)
        if (imageUri != null) {
            // 2. Criar um nome de arquivo único
            val fileName = "book_covers/${UUID.randomUUID()}.jpg"
            val coverRef = storage.reference.child(fileName)

            // 3. Fazer o upload do arquivo
            coverRef.putFile(imageUri)
                .addOnSuccessListener {
                    // 4. Upload bem-sucedido, agora pegar a URL de download
                    coverRef.downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            // 5. Salvar no Firestore com a URL da imagem
                            val livroComImagem = livro.copy(imageUrl = downloadUrl.toString())
                            saveBookToFirestore(livroComImagem, onSuccess, onError)
                        }
                        .addOnFailureListener { e ->
                            onError("Erro ao obter URL de download: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    onError("Erro no upload da imagem: ${e.message}")
                }
        } else {
            // 6. Nenhuma imagem nova fornecida (ex: editando apenas o texto)
            // Se o livro.id já existir, ele atualiza, senão cria um novo (sem imagem)
            saveBookToFirestore(livro, onSuccess, onError)
        }
    }

    /**
     * Função auxiliar que realmente salva/atualiza o documento no Firestore
     */
    private fun saveBookToFirestore(
        livro: Livro,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (livro.id.isEmpty()) {
            // Modo ADICIONAR
            booksCollection.add(livro)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Erro ao salvar livro.") }
        } else {
            // Modo EDITAR/ATUALIZAR
            // O ID já existe, então usamos .set() para sobrescrever o documento.
            booksCollection.document(livro.id).set(livro) // <-- ESTA É A LÓGICA DE ATUALIZAÇÃO
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Erro ao atualizar livro.") }
        }
    }

    /**
     * Exclui um livro do Firestore e sua imagem do Storage (UC3)
     */
    fun deleteBook(
        livro: Livro,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // 1. Excluir o documento do Firestore
        booksCollection.document(livro.id).delete()
            .addOnSuccessListener {
                // 2. Se a exclusão do Firestore for bem-sucedida,
                //    excluir a imagem do Storage.

                // Verifica se a imageUrl não está vazia e é uma URL do Firebase Storage
                if (livro.imageUrl.isNotEmpty() && livro.imageUrl.contains("firebasestorage.googleapis.com")) {
                    val imageRef = storage.getReferenceFromUrl(livro.imageUrl)
                    imageRef.delete()
                        .addOnSuccessListener {
                            // Imagem excluída com sucesso
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            // Erro ao excluir imagem, mas o livro foi excluído
                            Log.w("BookController", "Livro excluído, mas falha ao excluir imagem: ${e.message}")
                            onSuccess() // Ainda reporta sucesso, pois o principal (livro) foi excluído
                        }
                } else {
                    // Nenhuma imagem para excluir, apenas sucesso
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                onError("Erro ao excluir livro: ${e.message}")
            }
    }
}