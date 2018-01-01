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
        val file = File(SuggestionParserTest::class.java.classLoader
                .getResource("suggestion/google.xml").toURI())
        val xml = Okio.buffer(Okio.source(file)).let {
            val text = it.readUtf8()
            it.close()
            return@let text
        }
        val suggestions = SuggestionParser.parse(xml)
        assertEquals(10, suggestions.size)
    }

}