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
    fun getTitleId() {
        assertEquals(StartUp.BROWSER, StartUp.findByName("BROWSER"))
    }

    /**
     * Check of [StartUp#findById].
     */
    @Test
    fun getRadioButtonId() {
        assertEquals(StartUp.BROWSER, StartUp.findById(R.id.start_up_browser))
    }

}