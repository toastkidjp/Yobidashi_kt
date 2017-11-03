package jp.toastkid.yobidashi.browser.bookmark

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Test of [ExportedFileParser].
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class ExportedFileParserTest {

    /**
     * Check of {@link ExportedFileParser#parse}.
     */
    @Test
    fun test_parse() {
        val htmlFile = File(ExportedFileParserTest::class.java.classLoader
                .getResource("bookmark/sample.html").toURI())
        val bookmarks = ExportedFileParser.parse(htmlFile)
        //bookmarks.forEach { println(Moshi.Builder().build().adapter(Bookmark::class.java).toJson(it)) }
        assertEquals(19, bookmarks.size)
    }
}