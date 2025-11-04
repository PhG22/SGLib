package br.com.PhG22.sglib.model

// No pacote: model
data class Fine(
    val id: String = "",
    val userId: String = "",
    val reason: String = "", // Ex: "Late Return: The Ocean's Whisper"
    val amount: Double = 0.0,
    val isPaid: Boolean = false
)