package jp.toastkid.yobidashi.search.suggestion

import okio.Okio
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Test cases of [SuggestionParser].
 *
 * @author toastkidjp
 */
class SuggestionParserTest {

    /**
     * Check of [SuggestionParser.parse].
     */
    @Test
    fun test_parse() {
        val file = File(javaClass.classLoader?.getResource(PATH_TO_RESOURCE)?.toURI())
        val xml = Okio.buffer(Okio.source(file)).use { it.readUtf8() }
        val suggestions = SuggestionParser()(xml)
        assertEquals(10, suggestions.size)
    }

    companion object {
        private const val PATH_TO_RESOURCE = "suggestion/google.xml"
    }
}