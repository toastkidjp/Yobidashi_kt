package jp.toastkid.yobidashi.search

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [SearchQueryExtractor]'s test cases.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class SearchQueryExtractorTest {

    @Test
    fun testInvoke() {
        assertEquals(
                "かもめ",
                SearchQueryExtractor("https://www.google.com/search?q=%E3%81%8B%E3%82%82%E3%82%81")
        )
        assertEquals(
                "かもめ",
                SearchQueryExtractor("https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=%E3%81%8B%E3%82%82%E3%82%81")
        )
        assertEquals(
                "programmer",
                SearchQueryExtractor("https://www.job.com/search?query=programmer")
        )
    }
}