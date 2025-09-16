package jp.toastkid.chat.presentation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import jp.toastkid.chat.domain.model.ChatMessage
import jp.toastkid.chat.domain.model.GenerativeAiModel
import jp.toastkid.chat.domain.service.ChatService
import jp.toastkid.chat.infrastructure.service.ChatServiceImplementation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatTabViewModel(apiKey: String) {

    private val service: ChatService = ChatServiceImplementation(apiKey)

    private val textInput = mutableStateOf(TextFieldValue())

    private val focusRequester = FocusRequester()

    private val scrollState = LazyListState()

    private val connecting = mutableStateOf(false)

    fun messages(): List<ChatMessage> = service.messages()

    fun autoScrollingKey() = if (messages().isEmpty()) 0 else messages().last().text.length

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
        else -> jp.toastkid.lib.R.drawable.ic_clip
    }

    fun currentModelIcon() = modelIcon(currentModel.value)

    fun currentModelLabel() = currentModel.value.label()

    fun chooseModel(model: GenerativeAiModel) {
        currentModel.value = model
    }

    suspend fun send() {
        val text = textInput.value.text
        if (text.isBlank()) {
            return
        }

        textInput.value = TextFieldValue()

        connecting.value = true
        withContext(Dispatchers.IO) {
            service.send(currentModel.value, text)
        }
        connecting.value = false
    }

    fun textInput() = textInput.value

    fun onValueChanged(newValue: TextFieldValue) {
        textInput.value = newValue
    }

    fun focusRequester(): FocusRequester {
        return focusRequester
    }

    fun launch(
        //chat: Chat
    ) {
        //service.setChat(chat)
        focusRequester().requestFocus()
    }

    /*fun update(chatTab: ChatTab) {
        mainViewModel.replaceTab(chatTab,  ChatTab(service.getChat()))
    }*/

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

    fun exportableContent() = service.getChat().list().map { it.toString() }.toString()

}

private const val DEFAULT_LABEL = "Would you like to ask any question?"
