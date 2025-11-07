package br.com.PhG22.sglib.controller

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
import kotlin.math.ceil // <-- IMPORT NECESSÁRIO

object AdminController {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val loansCollection = db.collection("loans")
    private val booksCollection = db.collection("books")
    private val finesCollection = db.collection("fines")
    private val reviewsCollection = db.collection("reviews")

    // --- NOVA CONSTANTE ---
    // Defina aqui o valor da multa por dia de atraso
    private const val MULTA_POR_DIA = 2.50

    fun getPendingUsers(
        onSuccess: (List<Usuario>) -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection
            .whereEqualTo("cadastroAprovado", false)
            .get()
            .addOnSuccessListener { snapshot ->
                // Agora que o model Usuario tem @DocumentId, .toObjects() funciona
                val users = snapshot.toObjects(Usuario::class.java)
                onSuccess(users)
            }
            .addOnFailureListener { e ->
                onError("Erro ao buscar usuários pendentes: ${e.message}")
            }
    }

    fun approveUser(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection.document(userId)
            .update("cadastroAprovado", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao aprovar usuário: ${e.message}") }
    }

    fun denyUser(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        usersCollection.document(userId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao negar usuário: ${e.message}") }
    }

    fun getPendingLoans(
        onSuccess: (List<Emprestimo>) -> Unit,
        onError: (String) -> Unit
    ) {
        loansCollection
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                // Agora que o model Emprestimo tem @DocumentId, .toObjects() funciona
                val loans = snapshot.toObjects(Emprestimo::class.java)
                onSuccess(loans)
            }
            .addOnFailureListener { e -> onError("Erro ao buscar empréstimos: ${e.message}") }
    }

    fun approveLoan(
        loan: Emprestimo,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val bookRef = booksCollection.document(loan.bookId)
        val loanRef = loansCollection.document(loan.id)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 14)
        val dueDate = calendar.time

        db.runTransaction { transaction ->
            val bookSnapshot = transaction.get(bookRef)
            val currentQuantity = bookSnapshot.getLong("quantidade") ?: 0

            if (currentQuantity <= 0) {
                throw FirebaseFirestoreException(
                    "Livro indisponível!",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }

            transaction.update(bookRef, "quantidade", FieldValue.increment(-1))

            transaction.update(loanRef, mapOf(
                "status" to "approved",
                "dueDate" to dueDate
            ))

            null
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Falha na transação") }
    }

    fun denyLoan(
        loanId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        loansCollection.document(loanId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao negar empréstimo: ${e.message}") }
    }

    fun getPendingReturns(
        onSuccess: (List<Emprestimo>) -> Unit,
        onError: (String) -> Unit
    ) {
        loansCollection
            .whereEqualTo("status", "return_pending")
            .get()
            .addOnSuccessListener { snapshot ->
                // Agora que o model Emprestimo tem @DocumentId, .toObjects() funciona
                val loans = snapshot.toObjects(Emprestimo::class.java)
                onSuccess(loans)
            }
            .addOnFailureListener { e -> onError("Erro ao buscar devoluções: ${e.message}") }
    }

    /**
     * Confirma a devolução de um livro (UC5)
     * Lógica de multa (UC6) atualizada para valor dinâmico E ATRIBUIÇÃO DE ID.
     */
    fun confirmReturn(
        loan: Emprestimo,
        onSuccess: (isLate: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val bookRef = booksCollection.document(loan.bookId)
        val loanRef = loansCollection.document(loan.id)

        db.runTransaction { transaction ->
            transaction.update(loanRef, "status", "returned")
            transaction.update(bookRef, "quantidade", FieldValue.increment(1))

            val hoje = Date()
            val isLate = loan.dueDate != null && hoje.after(loan.dueDate)

            if (isLate) {
                // --- INÍCIO DA CORREÇÃO DO CRASH ---
                val diffInMillis = hoje.time - loan.dueDate!!.time
                val diasEmAtraso = ceil(diffInMillis.toDouble() / (1000 * 60 * 60 * 24)).toLong()
                val valorTotalMulta = diasEmAtraso * MULTA_POR_DIA

                // 1. Cria a referência do novo documento (o ID é gerado aqui)
                val newFineRef = finesCollection.document()

                // 2. Cria o objeto multa SEM o campo 'id'.
                // O @DocumentId no model irá preenchê-lo na LEITURA.
                val multa = Fine(
                    // id = newFineRef.id, // <-- REMOVER ESTA LINHA CAUSA O CRASH
                    userId = loan.userId,
                    reason = "Atraso de $diasEmAtraso dia(s): ${loan.bookTitle}",
                    amount = valorTotalMulta,
                    paid = false // <-- CORRIGIDO para 'paid'
                )
                // 3. Salva o objeto na referência
                transaction.set(newFineRef, multa)
                // --- FIM DA CORREÇÃO DO CRASH ---
            }

            isLate
        }
            .addOnSuccessListener { isLate ->
                onSuccess(isLate)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Falha ao confirmar devolução")
            }
    }

    fun getPendingReviews(
        onSuccess: (List<Resenha>) -> Unit,
        onError: (String) -> Unit
    ) {
        reviewsCollection
            .whereEqualTo("approved", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                // Agora que o model Resenha tem @DocumentId, .toObjects() funciona
                val reviews = snapshot.toObjects(Resenha::class.java)
                onSuccess(reviews)
            }
            .addOnFailureListener { e -> onError("Erro ao buscar resenhas: ${e.message}") }
    }

    fun approveReview(
        reviewId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        reviewsCollection.document(reviewId)
            .update("approved", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao aprovar resenha: ${e.message}") }
    }

    fun denyReview(
        reviewId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        reviewsCollection.document(reviewId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao excluir resenha: ${e.message}") }
    }

    fun getAllUnpaidFines(
        onSuccess: (List<Fine>) -> Unit,
        onError: (String) -> Unit
    ) {
        finesCollection
            .whereEqualTo("paid", false) // <-- CORRIGIDO para 'paid'
            .get()
            .addOnSuccessListener { snapshot ->
                // Agora que o model Fine tem @DocumentId, .toObjects() funciona
                val fines = snapshot.toObjects(Fine::class.java)
                onSuccess(fines)
            }
            .addOnFailureListener { e -> onError("Erro ao buscar multas: ${e.message}") }
    }

    fun markFineAsPaid(
        fineId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (fineId.isEmpty()) {
            onError("ID da multa está vazio. Não é possível atualizar.")
            return
        }
        finesCollection.document(fineId)
            .update("paid", true) // <-- CORRIGIDO para 'paid'
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Erro ao marcar multa como paga: ${e.message}") }
    }

    /**
     * Busca o nome de um usuário específico pelo seu UID.
     * Esta é a nova função auxiliar para corrigir o bug "(Carregando...)".
     */
    fun getUserNameById(
        userId: String,
        onSuccess: (String) -> Unit
    ) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                val nome = document.getString("nome")
                if (nome != null) {
                    onSuccess(nome)
                } else {
                    onSuccess("Usuário Desconhecido")
                }
            }
            .addOnFailureListener {
                onSuccess("Erro ao buscar") // Retorna uma string de erro
            }
    }
}