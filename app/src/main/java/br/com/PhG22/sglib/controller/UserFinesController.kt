package br.com.PhG22.sglib.controller

import br.com.PhG22.sglib.model.Fine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Este Controller é para o USUÁRIO ver as suas próprias multas
object UserFinesController {

    private val db = FirebaseFirestore.getInstance()
    private val finesCollection = db.collection("fines")
    private val auth = FirebaseAuth.getInstance()

    /**
     * Busca apenas as multas NÃO PAGAS do usuário atual (UC6 - Usuário)
     */
    fun getMyUnpaidFines(
        onSuccess: (List<Fine>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Usuário não autenticado.")
            return
        }

        finesCollection
            .whereEqualTo("userId", userId) // Filtra pelo ID do usuário logado
            .whereEqualTo("paid", false) // Filtra apenas por multas não pagas
            .get()
            .addOnSuccessListener { snapshot ->
                val fines = snapshot.toObjects(Fine::class.java)
                onSuccess(fines)
            }
            .addOnFailureListener { e -> onError("Erro ao buscar multas: ${e.message}") }
    }
}