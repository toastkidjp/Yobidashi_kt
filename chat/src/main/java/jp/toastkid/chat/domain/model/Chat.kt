/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
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
        val element = texts.last()
        if (texts.isEmpty() || element.role != "model") {
            texts.add(ChatMessage("model", textContent, image = imageContent))
            return
        }

        texts.set(
            texts.lastIndex,
            element.copy(
                text = element.text + textContent,
                image = imageContent,
                sources = element.sources
            )
        )

        if (text.sources().isNotEmpty()) {
            val element = texts.last()
            texts.set(texts.lastIndex, element.copy(sources = text.sources()))
        }

    }

    fun list(): List<ChatMessage> = texts

    fun clearMessages() {
        texts.clear()
    }

}