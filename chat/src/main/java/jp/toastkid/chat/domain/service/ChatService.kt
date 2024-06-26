package jp.toastkid.chat.domain.service

import jp.toastkid.chat.domain.model.Chat
import jp.toastkid.chat.domain.model.ChatMessage

interface ChatService {

    fun send(text: String): String?

    fun setChat(chat: Chat)

    fun getChat(): Chat

    fun messages(): List<ChatMessage>

}