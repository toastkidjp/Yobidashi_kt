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
        val element = texts.last()
        texts.set(texts.lastIndex, element.copy(text = element.text + text.message(), image = image))
    }

    fun list(): List<ChatMessage> = texts

    fun makeContent() = """
      {
        "contents": [
          ${texts.joinToString(",") { "{\"role\":\"${it.role}\", \"parts\":[ { \"text\": '${it.text.replace("\"", "\\\"").replace("'", "\'")}'} ]}" }}
        ],
        "safetySettings": [
            {
                "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                "threshold": "BLOCK_ONLY_HIGH"
            },
                        {
                            "category": "HARM_CATEGORY_HARASSMENT",
                            "threshold": "BLOCK_ONLY_HIGH"
                        },
                        {
                            "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                            "threshold": "BLOCK_ONLY_HIGH"
                        }
        ]
      }
    """.trimIndent()

}