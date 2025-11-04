package br.com.PhG22.sglib.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Resenha(
    @DocumentId
    val id: String = "",

    val bookId: String = "",
    val userId: String = "",
    val userName: String = "", // Para exibir quem escreveu

    val rating: Float = 0f, // Para as estrelas
    val comment: String = "",

    val isApproved: Boolean = false, // Começa como pendente

    @ServerTimestamp
    val timestamp: Date? = null
)