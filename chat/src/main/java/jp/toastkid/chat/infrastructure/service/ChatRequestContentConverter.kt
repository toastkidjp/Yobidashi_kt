package jp.toastkid.chat.infrastructure.service

import jp.toastkid.chat.domain.model.Chat
import jp.toastkid.chat.domain.model.ChatMessage

class ChatRequestContentConverter {

    operator fun invoke(
        chat: Chat,
        useImage: Boolean = false
    ) = """
      {
        "contents": [
          ${makeContents(chat.list())}
        ],
        ${if (useImage) "\"generationConfig\":{\"responseModalities\":[\"TEXT\",\"IMAGE\"]}," else "" }
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

    private fun makeContents(texts: List<ChatMessage>) =
        texts
            .filter { it.text.isNotBlank() }
            .joinToString(",", transform = ::toContent)

    private fun toContent(it: ChatMessage) =
        "{\"role\":\"${it.role}\", \"parts\":[ { \"text\": '${
            escape(it.text)
        }'}" +
                " ${
                    if (it.image.isNullOrBlank().not())
                        ",{\"inline_data\": {\"mime_type\":\"image/jpeg\", \"data\": \"${it.image}\"}}"
                    else
                        ""
                } ]}"

    private fun escape(text: String) =
        text.replace("\"", "\\\"").replace("'", "\\'")

}