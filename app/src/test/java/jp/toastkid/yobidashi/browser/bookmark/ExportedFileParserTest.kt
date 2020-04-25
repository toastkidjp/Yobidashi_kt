package jp.toastkid.yobidashi.browser.bookmark

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
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
     * Check of {@link ExportedFileParser#invoke}.
     */
    @Test
    fun testInvoke() {
        val resource = javaClass.classLoader
                ?.getResource("bookmark/sample.html")?.toURI() ?: return fail()
        val htmlFile = File(resource)
        val bookmarks = ExportedFileParser()(htmlFile)
        assertEquals(18, bookmarks.size)
    }
}