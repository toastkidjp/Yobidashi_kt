package jp.toastkid.yobidashi.wikipedia

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

/**
 * @author toastkidjp
 */
class UrlDeciderTest {

    @Test
    fun test() {
        assertTrue(UrlDecider().invoke().startsWith("https://${Locale.getDefault().language}."))
    }
}