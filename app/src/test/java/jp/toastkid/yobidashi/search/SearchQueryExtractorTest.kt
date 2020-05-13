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
        val searchQueryExtractor = SearchQueryExtractor()
        assertEquals(
                "かもめ",
                searchQueryExtractor("https://www.google.com/search?q=%E3%81%8B%E3%82%82%E3%82%81")
        )
        assertEquals(
                "かもめ",
                searchQueryExtractor("https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=%E3%81%8B%E3%82%82%E3%82%81")
        )
        assertEquals(
                "programmer",
                searchQueryExtractor("https://www.job.com/search?query=programmer")
        )
        assertEquals(
                "Noodle",
                searchQueryExtractor("https://commons.wikimedia.org/w/index.php?search=Noodle&ns0=1")
        )
        assertEquals(
                "スウィープ",
                searchQueryExtractor("https://ja.wikipedia.org/wiki/%E3%82%B9%E3%82%A6%E3%82%A3%E3%83%BC%E3%83%97")
        )
        assertEquals(
                "ラーメン",
                searchQueryExtractor("https://www.tumblr.com/search/%E3%83%A9%E3%83%BC%E3%83%A1%E3%83%B3")
        )
        assertEquals(
                "java",
                searchQueryExtractor("https://www.reddit.com/search/?q=java")
        )
        assertEquals(
                "kotlin",
                searchQueryExtractor("https://www.quora.com/search?q=kotlin")
        )
        assertEquals(
                "train",
                searchQueryExtractor("https://archive.org/search.php?query=train")
        )
        assertEquals(
                "tomato",
                searchQueryExtractor("https://search.naver.com/search.naver?ie=utf8&query=tomato")
        )
        assertEquals(
                "orange",
                searchQueryExtractor("https://search.daum.net/search?w=tot&q=orange")
        )
    }
}