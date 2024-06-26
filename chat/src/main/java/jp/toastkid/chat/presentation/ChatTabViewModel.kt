package jp.toastkid.chat.presentation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import jp.toastkid.chat.domain.model.ChatMessage
import jp.toastkid.chat.domain.service.ChatService
import jp.toastkid.chat.infrastructure.service.ChatServiceImplementation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatTabViewModel(apiKey: String) {

    private val service: ChatService = ChatServiceImplementation(apiKey)

    private val textInput = mutableStateOf(TextFieldValue())

    private val focusRequester = FocusRequester()

    private val scrollState = LazyListState()

    fun messages(): List<ChatMessage> = service.messages()

    suspend fun send() {
        val text = textInput.value.text
        if (text.isBlank()) {
            return
        }

        textInput.value = TextFieldValue()

        labelState.value = "Connecting in progress..."
        withContext(Dispatchers.IO) {
            service.send(text)
        }
        labelState.value = DEFAULT_LABEL
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

    private val labelState = mutableStateOf(DEFAULT_LABEL)

    fun label(): String {
        return labelState.value
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

private const val DEFAULT_LABEL = "Please would you input any sentences which you know something?"
