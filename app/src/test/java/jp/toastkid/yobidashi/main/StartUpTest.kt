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
        assertEquals(StartUp.BROWSER, StartUp.findByName(null))
        assertEquals(StartUp.BROWSER, StartUp.findByName(""))
        assertEquals(StartUp.SEARCH, StartUp.findByName("SEARCH"))
    }

    /**
     * Check of [StartUp#findById].
     */
    @Test
    fun testGetRadioButtonId() {
        assertEquals(StartUp.BROWSER, StartUp.findById(R.id.start_up_browser))
    }

}