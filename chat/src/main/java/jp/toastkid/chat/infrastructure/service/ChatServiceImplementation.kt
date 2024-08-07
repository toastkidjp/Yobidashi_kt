package jp.toastkid.chat.infrastructure.service

import jp.toastkid.chat.domain.model.Chat
import jp.toastkid.chat.domain.model.ChatMessage
import jp.toastkid.chat.domain.repository.ChatRepository
import jp.toastkid.chat.domain.service.ChatService
import jp.toastkid.chat.infrastructure.repository.ChatApi
import java.util.concurrent.atomic.AtomicReference

class ChatServiceImplementation(apiKey: String) : ChatService {

    private val chatHolder: AtomicReference<Chat> = AtomicReference(Chat())

    private val repository: ChatRepository = ChatApi(apiKey)

    override fun send(text: String): String? {
        val chat = chatHolder.get()
        chat.addUserText(text)

        repository.request(chat.makeContent()) {
            if (it == null) {
                return@request
            }

            chat.addModelText(it)
        }

        return null
    }

    override fun setChat(chat: Chat) {
        chatHolder.set(chat)
    }

    override fun getChat(): Chat {
        return chatHolder.get()
    }

    override fun messages(): List<ChatMessage> {
        return chatHolder.get().list()
    }

}