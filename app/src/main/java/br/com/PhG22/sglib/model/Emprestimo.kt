package br.com.PhG22.sglib.model
// No pacote: model
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Emprestimo(
    @DocumentId
    val id: String = "",

    val bookId: String = "",
    val bookTitle: String = "",
    val bookImageUrl: String = "",

    val userId: String = "",

    // Status: "pending", "approved", "returned", "overdue"
    val status: String = "pending", // Empréstimo começa como pendente

    @ServerTimestamp // Pega a data/hora do servidor
    val requestDate: Date? = null,

    val dueDate: Date? = null // Data de devolução (a ser preenchida pelo admin)
)