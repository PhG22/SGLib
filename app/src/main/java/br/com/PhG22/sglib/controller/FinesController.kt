package br.com.PhG22.sglib.controller
// No pacote: controller
import br.com.PhG22.sglib.model.Fine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FinesController {

    private val db = FirebaseFirestore.getInstance()
    private val finesCollection = db.collection("fines")
    private val auth = FirebaseAuth.getInstance()

    fun getMyUnpaidFines(
        onSuccess: (List<Fine>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        finesCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isPaid", false) // Apenas multas não pagas
            .get()
            .addOnSuccessListener { snapshot ->
                val fines = snapshot.toObjects(Fine::class.java)
                onSuccess(fines)
            }
            .addOnFailureListener { e -> onError(e.message ?: "Erro ao buscar multas.") }
    }
}