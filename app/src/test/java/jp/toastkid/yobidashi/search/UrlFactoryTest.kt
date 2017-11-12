package jp.toastkid.yobidashi.search

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

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
        assertEquals(
                "https://www.google.com/search?q=tomato",
                UrlFactory.make(RuntimeEnvironment.application, "GOOGLE", "tomato").toString()
        )
        assertEquals(
                "https://www.google.com/search?q=tomato",
                UrlFactory.make(RuntimeEnvironment.application, "tomato", "tomato").toString()
        )
    }

}