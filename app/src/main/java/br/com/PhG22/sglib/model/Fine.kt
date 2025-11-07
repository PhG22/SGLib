package br.com.PhG22.sglib.model

import com.google.firebase.firestore.DocumentId

data class Fine(
    @DocumentId // <-- Isto diz ao Firestore: "Coloca o ID do DOCUMENTO aqui"
    val id: String = "",
    val userId: String = "",
    val reason: String = "",
    val amount: Double = 0.0,
    val paid: Boolean = false // <-- CORRIGIDO de "isPaid" para "paid"
)