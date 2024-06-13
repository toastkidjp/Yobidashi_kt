package jp.toastkid.chat.infrastructure.service

import org.json.JSONObject
import java.util.regex.Pattern

class ChatStreamParser {

    operator fun invoke(line: String): String? {
        if (line.isBlank()) {
            return null
        }

        return JSONObject(line.substring(5))
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

}

private val pattern = Pattern.compile("\\{\"parts\": \\[\\{\"text\": \"(.+?)\"\\}\\]", Pattern.DOTALL)
