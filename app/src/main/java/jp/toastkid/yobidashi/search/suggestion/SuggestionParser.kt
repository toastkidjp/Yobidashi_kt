package jp.toastkid.yobidashi.search.suggestion

import java.util.*
import java.util.regex.Pattern

/**
 * Suggestion response parser.
 *
 * @author toastkidjp
 */
internal class SuggestionParser {

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
