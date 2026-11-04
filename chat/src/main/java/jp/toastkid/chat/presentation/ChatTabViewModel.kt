/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.chat.presentation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import jp.toastkid.chat.R
import jp.toastkid.chat.domain.model.ChatMessage
import jp.toastkid.chat.domain.model.GenerativeAiModel
import jp.toastkid.chat.domain.service.ChatService
import jp.toastkid.chat.infrastructure.service.ChatServiceImplementation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatTabViewModel(apiKey: String) {

    private val service: ChatService = ChatServiceImplementation(apiKey)

    private val textInput = TextFieldState()

    private val focusRequester = FocusRequester()

    private val scrollState = LazyListState()

    private val connecting = mutableStateOf(false)

    fun messages(): List<ChatMessage> = service.messages()

    private val openModelChooser = mutableStateOf(false)

    fun openModelChooser() {
        openModelChooser.value = true
    }

    fun closeModelChooser() {
        openModelChooser.value = false
    }

    fun openingModelChooser(): Boolean {
        return openModelChooser.value
    }

    private val currentModel = mutableStateOf(GenerativeAiModel.GEMINI_2_5_FLASH_LITE)

    fun modelIcon(model: GenerativeAiModel) = when (model) {
        else -> R.drawable.ic_agent
    }

    fun currentModelIcon() = modelIcon(currentModel.value)

    fun currentModelLabel() = currentModel.value.label()

    fun currentModelVersion() = currentModel.value.version()

    fun chooseModel(model: GenerativeAiModel) {
        currentModel.value = model
    }

    suspend fun send() {
        val text = textInput.text.toString()
        if (text.isBlank()) {
            return
        }

        textInput.clearText()

        connecting.value = true
        withContext(Dispatchers.IO) {
            service.send(currentModel.value, text)
        }
        connecting.value = false
    }

    fun textInput() = textInput

    fun clearInput() {
        textInput.clearText()
    }

    fun focusRequester(): FocusRequester {
        return focusRequester
    }

    fun launch() {
        focusRequester().requestFocus()
    }

    fun label(): String {
        return if (connecting.value) "Connecting in progress..." else DEFAULT_LABEL
    }

    fun name(role: String): String {
        return when (role) {
            "user" -> "You"
            "model" -> "Assistant"
            else -> "Unknown"
        }
    }

    fun scrollState(): LazyListState {
        return scrollState
    }

    fun nameColor(role: String): Color {
        return Color(if (role == "model") 0xFF86EEC7 else 0xFFFFD54F)
    }

    fun exportableContent() = service.getChat().list().map(ChatMessage::toString).toString()

    fun clearChat() {
        service.clearChat()
    }

}

private const val DEFAULT_LABEL = "Would you like to ask any question?"
