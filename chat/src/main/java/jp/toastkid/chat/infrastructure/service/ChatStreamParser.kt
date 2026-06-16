/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.chat.infrastructure.service

import jp.toastkid.chat.domain.model.Source
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

        val content = firstCandidate.getJSONObject("content")
        val firstPart =
            if (content.has("parts")) content.getJSONArray("parts").getJSONObject(0)
            else null
        val message =
            if (firstPart?.has("text") == true)
                firstPart
                    .getString("text")
                    .replace("\\n", "\n")
                    .replace("\\u003e", ">")
                    .replace("\\u003c", "<")
            else
                null

        val sources = mutableListOf<Source>()
        if (line.contains("groundingChunks")) {
            val sourceMatcher = sourcePattern.matcher(line)
            while (sourceMatcher.find()) {
                sources.add(
                    Source(
                        sourceMatcher.group(2) ?: "",
                        sourceMatcher.group(1) ?: ""
                    )
                )
            }
        }

        val imageMatcher = imagePattern.matcher(line)
        val base64 = if (imageMatcher.find()) imageMatcher.group(2) else null

        if (message.isNullOrEmpty() && base64.isNullOrEmpty() && sources.isEmpty()) {
            return null
        }

        return ChatResponseItem(
            message = message ?: base64 ?: "",
            image = base64 != null,
            sources = sources
        )
    }

    private val imagePattern = Pattern.compile("\"inlineData\":(.+?)\"data\": \"(.+?)\"", Pattern.DOTALL)

    private val sourcePattern = Pattern.compile("\\{\"web\": \\{\"uri\": \"(.+?)\",\"title\": \"(.+?)\"\\}\\}", Pattern.DOTALL)

}
