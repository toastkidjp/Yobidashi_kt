package jp.toastkid.yobidashi.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test of [UserAgent].
 *
 * @author toastkidjp
 */
class UserAgentTest {

    @Test
    fun test() {
        assertEquals("Android", UserAgent.ANDROID.title())
        assertTrue(UserAgent.ANDROID.text().isNotEmpty())
    }
}