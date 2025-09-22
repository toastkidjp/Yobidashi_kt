package jp.toastkid.chat.domain.model

import androidx.compose.runtime.mutableStateListOf
import jp.toastkid.chat.domain.repository.ChatResponseItem

data class Chat(private val texts: MutableList<ChatMessage> = mutableStateListOf()) {

    fun addUserText(text: String) {
        texts.add(ChatMessage("user", text))
    }

    fun addModelText(text: ChatResponseItem) {
        val imageContent = if (text.image()) text.message() else null
        val textContent = if (text.image()) "" else text.message()
        if (texts.isEmpty() || texts.last().role != "model") {
            texts.add(ChatMessage("model", textContent, image = imageContent))
            return
        }

        val element = texts.last()
        texts.set(texts.lastIndex, element.copy(text = element.text + textContent, image = imageContent))
    }

    fun list(): List<ChatMessage> = texts

    fun clearMessages() {
        texts.clear()
    }

}