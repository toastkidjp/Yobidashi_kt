package jp.toastkid.yobidashi.browser.webview

import android.os.Bundle
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class LongTapItemHolderTest {

    private lateinit var longTapItemHolder: LongTapItemHolder

    @Before
    fun setUp() {
        longTapItemHolder = LongTapItemHolder()
    }

    @Test
    fun reset() {
        longTapItemHolder.title = "title"
        longTapItemHolder.anchor = "https://test.com"

        longTapItemHolder.reset()

        assertTrue(longTapItemHolder.title.isEmpty())
        assertTrue(longTapItemHolder.anchor.isEmpty())
    }

    @Test
    fun extract() {
        val bundle = mockk<Bundle>()
        every { bundle.get("title") }.returns("test_title")
        every { bundle.get("url") }.returns("https://test.com")

        longTapItemHolder.extract(bundle)

        assertEquals("test_title", longTapItemHolder.title)
        assertEquals("https://test.com", longTapItemHolder.anchor)
    }

    @Test
    fun extractTrim() {
        val bundle = mockk<Bundle>()
        every { bundle.get("title") }.returns("  test_title  ")
        every { bundle.get("url") }.returns("https://test.com")

        longTapItemHolder.extract(bundle)

        assertEquals("test_title", longTapItemHolder.title)
        assertEquals("https://test.com", longTapItemHolder.anchor)
    }
}