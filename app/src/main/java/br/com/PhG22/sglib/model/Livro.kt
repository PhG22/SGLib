package br.com.PhG22.sglib.model

// No pacote: model
import com.google.firebase.firestore.DocumentId

data class Livro(
    @DocumentId // Anotação para pegar o ID automático do documento
    val id: String = "",

    val titulo: String = "",
    val autor: String = "",
    val anoPublicacao: Int = 0,
    val editora: String = "",
    val quantidade: Int = 0, // Quantidade total no acervo
    val imageUrl: String = ""  // URL da imagem da capa
)