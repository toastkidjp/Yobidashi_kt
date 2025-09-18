package jp.toastkid.chat.infrastructure.service

import jp.toastkid.chat.domain.repository.ChatResponseItem
import org.json.JSONObject
import java.util.regex.Pattern

class ChatStreamParser {

    operator fun invoke(line: String): ChatResponseItem? {
        if (line.isBlank() || line.startsWith("data:").not()) {
            return null
        }

        val firstCandidate = JSONObject(line.substring(5))
            .getJSONArray("candidates")
            .getJSONObject(0)

        if (firstCandidate.has("finishReason")) {
            val finishReason = firstCandidate.getString("finishReason")
            if (finishReason == "SAFETY" || finishReason == "OTHER") {
                return ChatResponseItem.error()
            }
        }

        if (!firstCandidate.has("content")) {
            return null
        }

        val firstPart = firstCandidate
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
        val message = if (firstPart.has("text"))
            firstPart
                .getString("text")
                .replace("\\n", "\n")
                .replace("\\u003e", ">")
                .replace("\\u003c", "<")
        else ""

        val imageMatcher = imagePattern.matcher(line)
        val base64 = if (imageMatcher.find()) imageMatcher.group(2) else null

        val messageText = message ?: base64 ?: ""
        if (messageText.isEmpty()) {
            return null
        }

        return ChatResponseItem(
            message = messageText,
            image = base64 != null
        )
    }

    private val imagePattern = Pattern.compile("\"inlineData\":(.+?)\"data\": \"(.+?)\"", Pattern.DOTALL)

}
