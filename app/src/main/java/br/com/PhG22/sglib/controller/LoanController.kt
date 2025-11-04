package br.com.PhG22.sglib.controller
// No pacote: controller
import br.com.PhG22.sglib.model.Emprestimo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object LoanController {

    private val db = FirebaseFirestore.getInstance()
    private val loansCollection = db.collection("loans")
    private val auth = FirebaseAuth.getInstance()

    fun getMyLoans(
        onSuccess: (List<Emprestimo>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Usuário não autenticado.")
            return
        }

        loansCollection
            .whereEqualTo("userId", userId)
            .orderBy("requestDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val loans = snapshot.toObjects(Emprestimo::class.java)
                onSuccess(loans)
            }
            .addOnFailureListener { e -> onError(e.message ?: "Erro ao buscar empréstimos.") }
    }

    // UC5: Solicitar Devolução
    fun requestReturn(
        loanId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        loansCollection.document(loanId)
            .update("status", "return_pending") // Pede confirmação do Admin
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erro ao solicitar devolução.") }
    }
}