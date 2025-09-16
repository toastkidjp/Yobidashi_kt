package jp.toastkid.chat.domain.model

data class ChatMessage(
    val role: String,
    val text: String,
    val image: String? = null
)