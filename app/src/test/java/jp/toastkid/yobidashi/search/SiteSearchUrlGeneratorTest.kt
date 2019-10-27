package jp.toastkid.yobidashi.search

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class SiteSearchUrlGeneratorTest {

    /**
     * Testing object.
     */
    private val siteSearchUrlGenerator = SiteSearchUrlGenerator()

    /**
     * Check behavior.
     */
    @Test
    fun test() {
        assertEquals(
                "https://www.google.com/search?as_dt=i&as_sitesearch=www.yahoo.com&as_q=Orange",
                siteSearchUrlGenerator("https://www.yahoo.com", "Orange")
        )
    }
}