package jp.toastkid.chat.domain.model

import androidx.compose.runtime.mutableStateListOf
import jp.toastkid.chat.domain.repository.ChatResponseItem

data class Chat(private val texts: MutableList<ChatMessage> = mutableStateListOf()) {

    fun addUserText(text: String) {
        texts.add(ChatMessage("user", text))
    }

    fun addModelText(text: ChatResponseItem) {
        if (texts.isEmpty() || texts.last().role != "model") {
            if (text.image()) {
                texts.add(ChatMessage("model", text = "", image = text.message()))
                return
            }
            texts.add(ChatMessage("model", text.message()))
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