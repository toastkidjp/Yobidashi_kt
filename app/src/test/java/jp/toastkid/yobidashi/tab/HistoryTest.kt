package jp.toastkid.yobidashi.tab

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [History]'s test cases.
 *
 * @author toastkidjp
 */
class HistoryTest {

    /**
     * Test of making object.
     */
    @Test
    fun test() {
        val history = History("title", "https://www.google.com")
        assertEquals("title", history.title())
        assertEquals("https://www.google.com", history.url())
        assertEquals(0, history.scrolled)
    }

    /**
     * Test of empty object.
     */
    @Test
    fun test_empty() {
        val empty = History.EMPTY
        assertEquals("", empty.title())
        assertEquals("", empty.url())
        assertEquals(0,  empty.scrolled)
    }
}