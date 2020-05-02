package jp.toastkid.yobidashi.main

import jp.toastkid.yobidashi.R
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [StartUp]'s test cases.
 *
 * @author toastkidjp
 */
class StartUpTest {

    /**
     * Check of [StartUp#find].
     */
    @Test
    fun testGetTitleId() {
        assertEquals(StartUp.SEARCH, StartUp.findByName(null))
        assertEquals(StartUp.SEARCH, StartUp.findByName(""))
        assertEquals(StartUp.SEARCH, StartUp.findByName("SEARCH"))
        assertEquals(StartUp.BROWSER, StartUp.findByName("BROWSER"))
        assertEquals(StartUp.BOOKMARK, StartUp.findByName("BOOKMARK"))
    }

    /**
     * Check of [StartUp#findById].
     */
    @Test
    fun testGetRadioButtonId() {
        assertEquals(StartUp.BROWSER, StartUp.findById(R.id.start_up_browser))
    }

}