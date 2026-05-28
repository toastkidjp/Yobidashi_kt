/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.chat.infrastructure.service

import jp.toastkid.chat.domain.model.Chat
import jp.toastkid.chat.domain.model.ChatMessage
import jp.toastkid.chat.domain.model.GenerativeAiModel
import jp.toastkid.chat.domain.repository.ChatRepository
import jp.toastkid.chat.domain.service.ChatService
import jp.toastkid.chat.infrastructure.repository.ChatApi
import java.util.concurrent.atomic.AtomicReference

class ChatServiceImplementation(apiKey: String) : ChatService {

    private val chatHolder: AtomicReference<Chat> = AtomicReference(Chat())

    private val converter: ChatRequestContentConverter = ChatRequestContentConverter()

    private val repositories: Map<GenerativeAiModel, ChatRepository> =
        GenerativeAiModel.entries.associateWith { ChatApi(apiKey, it.url()) }

    override fun send(
        model: GenerativeAiModel,
        text: String
    ): String? {
        val chat = chatHolder.get()
        chat.addUserText(text)

        repositories.get(model)?.request(converter(chat, model.webGrounding(), model.image())) {
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

    override fun clearChat() {
        chatHolder.get().clearMessages()
    }

}