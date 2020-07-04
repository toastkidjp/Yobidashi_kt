package jp.toastkid.search

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
        val urlFactory = UrlFactory()
        assertEquals(
                "https://www.google.com/search?q=tomato",
                urlFactory(RuntimeEnvironment.application, "GOOGLE", "tomato").toString()
        )
        assertEquals(
                "https://www.google.com/search?q=tomato",
                urlFactory(RuntimeEnvironment.application, "tomato", "tomato").toString()
        )
    }

}