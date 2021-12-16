/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.suggestion

import java.util.ArrayList
import java.util.regex.Pattern

/**
 * Suggestion response parser.
 *
 * @author toastkidjp
 */
class SuggestionParser {

    /**
     * Parse response xml.
     * @param response
     *
     * @return suggest words
     */
    operator fun invoke(response: String): List<String> {
        val split = response.split("</CompleteSuggestion>")
        val suggestions = ArrayList<String>(split.size)

        split.forEach {
            val matcher = PATTERN.matcher(it)
            if (matcher.find()) {
                val element = matcher.group(1) ?: return@forEach
                suggestions.add(element)
            }
        }
        return suggestions
    }

    companion object {

        /**
         * For extracting suggested word.
         */
        private val PATTERN: Pattern = Pattern.compile("<suggestion data=\"(.+?)\"/>", Pattern.DOTALL)

    }
}
