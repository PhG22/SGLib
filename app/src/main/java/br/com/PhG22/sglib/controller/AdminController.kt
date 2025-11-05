package br.com.PhG22.sglib.controller
// No pacote: controller
import android.util.Log
import br.com.PhG22.sglib.model.Emprestimo
import br.com.PhG22.sglib.model.Fine
import br.com.PhG22.sglib.model.Resenha
import br.com.PhG22.sglib.model.Usuario
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.Date

object AdminController {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val loansCollection = db.collection("loans")
    private val booksCollection = db.collection("books")
    private val finesCollection = db.collection("fines")
    private val reviewsCollection = db.collection("reviews")

    /**
     * Busca todos os usuários pendentes de aprovação (UC2)
     */
    fun getPendingUsers(
        onSuccess: (List<Usuario>) -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection
            .whereEqualTo("cadastroAprovado", false) // Filtra por usuários não aprovados
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.toObjects(Usuario::class.java)
                onSuccess(users)
            }
            .addOnFailureListener { e ->
                onError("Erro ao buscar usuários pendentes: ${e.message}")
            }
    }

    /**
     * Aprova o cadastro de um usuário (UC2)
     */
    fun approveUser(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection.document(userId)
            .update("cadastroAprovado", true) // Muda o status para aprovado
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao aprovar usuário: ${e.message}") }
    }

    /**
     * Nega (exclui) o cadastro de um usuário (UC2)
     * * Nota: Isso exclui o registro do Firestore. O registro de Auth ficará órfão,
     * mas nosso 'AuthController' já impede o login se o registro do Firestore não existir.
     */
    fun denyUser(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection.document(userId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao negar usuário: ${e.message}") }
    }

    /**
     * Busca todos os empréstimos pendentes (UC4)
     */
    fun getPendingLoans(
        onSuccess: (List<Emprestimo>) -> Unit,
        onError: (String) -> Unit
    ) {
        loansCollection
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                val loans = snapshot.toObjects(Emprestimo::class.java)
                onSuccess(loans)
            }
            .addOnFailureListener { e -> onError("Erro ao buscar empréstimos: ${e.message}") }
    }

    /**
     * Aprova um empréstimo usando uma transação (UC4)
     */
    fun approveLoan(
        loan: Emprestimo,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val bookRef = booksCollection.document(loan.bookId)
        val loanRef = loansCollection.document(loan.id)

        // Calcula a data de devolução (ex: 14 dias a partir de hoje)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 14)
        val dueDate = calendar.time

        // 1. Inicia a Transação
        db.runTransaction { transaction ->
            val bookSnapshot = transaction.get(bookRef)
            val currentQuantity = bookSnapshot.getLong("quantidade") ?: 0

            // 2. Verifica o estoque
            if (currentQuantity <= 0) {
                throw FirebaseFirestoreException(
                    "Livro indisponível!",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            // 3. Atualiza o estoque do livro (decrementa 1)
            transaction.update(bookRef, "quantidade", FieldValue.increment(-1))

            // 4. Atualiza o empréstimo
            transaction.update(loanRef, mapOf(
                "status" to "approved",
                "dueDate" to dueDate
            ))

            null // Transação retorna null em caso de sucesso
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Falha na transação") }
    }

    /**
     * Nega (exclui) uma solicitação de empréstimo (UC4)
     */
    fun denyLoan(
        loanId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Simplesmente exclui o documento de solicitação
        loansCollection.document(loanId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao negar empréstimo: ${e.message}") }
    }

    /**
     * Busca todas as devoluções pendentes de confirmação (UC5)
     */
    fun getPendingReturns(
        onSuccess: (List<Emprestimo>) -> Unit,
        onError: (String) -> Unit
    ) {
        loansCollection
            .whereEqualTo("status", "return_pending") // Solicitado pelo usuário
            .get()
            .addOnSuccessListener { snapshot ->
                val loans = snapshot.toObjects(Emprestimo::class.java)
                onSuccess(loans)
            }
            .addOnFailureListener { e -> onError("Erro ao buscar devoluções: ${e.message}") }
    }

    /**
     * Confirma a devolução de um livro (UC5)
     * Também verifica se há atraso e cria a multa (UC6)
     */
    fun confirmReturn(
        loan: Emprestimo,
        onSuccess: (isLate: Boolean) -> Unit, // Retorna true se uma multa foi gerada
        onError: (String) -> Unit
    ) {
        val bookRef = booksCollection.document(loan.bookId)
        val loanRef = loansCollection.document(loan.id)

        db.runTransaction { transaction ->
            // 1. Atualiza o status do empréstimo para "devolvido"
            transaction.update(loanRef, "status", "returned")

            // 2. Devolve o livro ao estoque
            transaction.update(bookRef, "quantidade", FieldValue.increment(1))

            // 3. Lógica de Multa (UC6)
            val hoje = Date()
            val isLate = loan.dueDate != null && hoje.after(loan.dueDate)

            if (isLate) {
                // O livro está atrasado. Cria um novo documento de multa.
                val newFineRef = finesCollection.document()
                val multa = Fine(
                    userId = loan.userId,
                    reason = "Atraso na devolução: ${loan.bookTitle}",
                    amount = 10.0, // Valor fixo da multa (exemplo)
                    isPaid = false
                )
                transaction.set(newFineRef, multa)
            }

            isLate // Retorna o status de atraso
        }
            .addOnSuccessListener { isLate ->
                onSuccess(isLate) // Sucesso na transação
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Falha ao confirmar devolução")
            }
    }

    /**
     * Busca todas as resenhas pendentes de aprovação (UC8)
     */
    fun getPendingReviews(
        onSuccess: (List<Resenha>) -> Unit,
        onError: (String) -> Unit
    ) {
        reviewsCollection
            .whereEqualTo("approved", false) // <-- Usando o campo "approved" que definimos
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val reviews = snapshot.toObjects(Resenha::class.java)
                onSuccess(reviews)
            }
            .addOnFailureListener { e -> onError("Erro ao buscar resenhas: ${e.message}") }
    }

    /**
     * Aprova uma resenha (UC8) [cite: 388]
     */
    fun approveReview(
        reviewId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        reviewsCollection.document(reviewId)
            .update("approved", true) // <-- Usando o campo "approved"
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao aprovar resenha: ${e.message}") }
    }

    /**
     * Nega (exclui) uma resenha (UC8) [cite: 388]
     */
    fun denyReview(
        reviewId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        reviewsCollection.document(reviewId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao excluir resenha: ${e.message}") }
    }

    /**
     * Busca todas as multas não pagas de todos os usuários (UC6)
     */
    fun getAllUnpaidFines(
        onSuccess: (List<Fine>) -> Unit,
        onError: (String) -> Unit
    ) {
        finesCollection
            .whereEqualTo("isPaid", false) // Filtra apenas por multas pendentes
            .get()
            .addOnSuccessListener { snapshot ->
                val fines = snapshot.toObjects(Fine::class.java)
                onSuccess(fines)
            }
            .addOnFailureListener { e -> onError("Erro ao buscar multas: ${e.message}") }
    }

    /**
     * Marca uma multa como paga (UC6)
     * Esta é a "confirmação" do pagamento
     */
    fun markFineAsPaid(
        fineId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        finesCollection.document(fineId)
            .update("isPaid", true) // Define o status da multa como 'paga'
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao marcar multa como paga: ${e.message}") }
    }
}