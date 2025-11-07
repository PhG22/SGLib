package br.com.PhG22.sglib.model

data class Usuario(
    val uid: String = "", // ID do Firebase Auth
    val nome: String = "", // [cite: 52]
    val email: String = "", // [cite: 50]
    val telefone: String = "", // [cite: 53]
    val fotoUrl: String = "", //
    val cadastroAprovado: Boolean = false // [cite: 124]
)