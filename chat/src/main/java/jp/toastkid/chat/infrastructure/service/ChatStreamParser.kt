package jp.toastkid.chat.infrastructure.service

import org.json.JSONObject

class ChatStreamParser {

    operator fun invoke(line: String): String? {
        if (line.isBlank()) {
            return null
        }

        val firstCandidate = JSONObject(line.substring(5))
            .getJSONArray("candidates")
            .getJSONObject(0)

        if (!firstCandidate.has("content")) {
            return null
        }

        return firstCandidate
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

}
