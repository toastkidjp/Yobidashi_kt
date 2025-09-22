package jp.toastkid.chat.domain.model

import androidx.compose.runtime.mutableStateListOf
import jp.toastkid.chat.domain.repository.ChatResponseItem

data class Chat(private val texts: MutableList<ChatMessage> = mutableStateListOf()) {

    fun addUserText(text: String) {
        texts.add(ChatMessage("user", text))
    }

    fun addModelText(text: ChatResponseItem) {
        if (texts.isEmpty() || texts.last().role != "model") {
            val content = if (text.image()) "" else text.message()
            val imageContent = if (text.image()) text.message() else null
            texts.add(ChatMessage("model", content, image = imageContent))
            return
        }

        val image = if (text.image()) text.message() else null
        val append = if (text.image()) "" else text.message()
        val element = texts.last()
        texts.set(texts.lastIndex, element.copy(text = element.text + append, image = image))
    }

    fun list(): List<ChatMessage> = texts

    fun clearMessages() {
        texts.clear()
    }

}