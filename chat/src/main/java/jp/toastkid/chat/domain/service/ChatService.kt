package jp.toastkid.chat.domain.service

import jp.toastkid.chat.domain.model.Chat
import jp.toastkid.chat.domain.model.ChatMessage
import jp.toastkid.chat.domain.model.GenerativeAiModel

interface ChatService {

    fun send(model: GenerativeAiModel, text: String): String?

    fun setChat(chat: Chat)

    fun getChat(): Chat

    fun messages(): List<ChatMessage>

    fun clearChat()

}