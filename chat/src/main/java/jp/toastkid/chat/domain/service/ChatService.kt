/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
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