package jp.toastkid.jitte.search.suggestion

import java.util.ArrayList
import java.util.regex.Pattern

/**
 * @author toastkidjp
 */
internal class SuggestionParser {

    /**
     * Parse response xml.
     * @param response
     * *
     * @return suggest words
     */
    fun parse(response: String): List<String> {
        val split = response.split("</CompleteSuggestion>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val suggestions = ArrayList<String>(split.size)
        for (line in split) {
            val matcher = PATTERN.matcher(line)
            if (matcher.find()) {
                suggestions.add(matcher.group(1))
            }
        }
        return suggestions
    }

    companion object {

        /** For extracting suggested word.  */
        private val PATTERN = Pattern.compile("<suggestion data=\"(.+?)\"/>", Pattern.DOTALL)
    }
}
