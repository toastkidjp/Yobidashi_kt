package jp.toastkid.yobidashi.wikipedia

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

/**
 * [UrlDecider]'s test case.
 *
 * @author toastkidjp
 */
class UrlDeciderTest {

    /**
     * Check for generating URL.
     */
    @Test
    fun test() {
        assertTrue(UrlDecider().invoke().startsWith("https://${Locale.getDefault().language}."))
    }
}