package jp.toastkid.yobidashi.wikipedia

import jp.toastkid.yobidashi.wikipedia.random.UrlDecider
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

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