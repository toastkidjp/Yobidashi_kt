package jp.toastkid.search

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test cases of [UrlFactory].
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class UrlFactoryTest {

    /**
     * Check of [UrlFactory.make].
     */
    @Test
    fun test_make() {
        val urlFactory = UrlFactory()
        assertEquals(
                "https://www.google.com/search?q=tomato",
                urlFactory("GOOGLE", "tomato").toString()
        )
        assertEquals(
                "https://www.google.com/search?q=tomato",
                urlFactory("tomato", "tomato").toString()
        )
    }

}