package jp.toastkid.search

import org.junit.Assert
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
    private val siteSearchUrlGenerator = jp.toastkid.search.SiteSearchUrlGenerator()

    /**
     * Check behavior.
     */
    @Test
    fun test() {
        repeat(10) {
            Assert.assertEquals(
                    "https://www.google.com/search?as_dt=i&as_sitesearch=www.yahoo.com&as_q=Orange",
                    siteSearchUrlGenerator("https://www.yahoo.com", "Orange")
            )
        }
    }
}