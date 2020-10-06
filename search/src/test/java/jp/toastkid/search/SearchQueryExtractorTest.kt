package jp.toastkid.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
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

    private lateinit var searchQueryExtractor: SearchQueryExtractor

    @Before
    fun setUp() {
        searchQueryExtractor = SearchQueryExtractor()
    }

    @Test
    fun testNull() {
        assertNull(searchQueryExtractor("/search?q=orange"))
    }

    @Test
    fun testInvoke() {
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
        assertEquals(
                "orange",
                searchQueryExtractor("https://www.wolframalpha.com/input/?i=orange")
        )
        assertEquals(
                "Yahoo",
                searchQueryExtractor("https://www.linkedin.com/jobs/search?keywords=Yahoo")
        )
        assertEquals(
                "Yahoo",
                searchQueryExtractor("https://www.ft.com/search?q=Yahoo")
        )
        assertEquals(
                "Yahoo",
                searchQueryExtractor("https://web.archive.org/web/*/Yahoo")
        )
        assertEquals(
                "tomato",
                searchQueryExtractor("https://www.qwant.com/?q=tomato")
        )
        assertEquals(
                "tomato",
                searchQueryExtractor("https://www.startpage.com/sp/search?q=tomato")
        )
        assertEquals(
                "poirot",
                searchQueryExtractor("https://www.imdb.com/find?q=poirot")
        )
        assertEquals(
                "poirot",
                searchQueryExtractor("https://m.facebook.com/public/poirot")
        )
        assertEquals(
                "Soccer",
                searchQueryExtractor("https://www.espn.com/search/_/q/Soccer")
        )
        assertEquals(
                "ラーメン",
                searchQueryExtractor("https://duckduckgo.com/?q=%E3%83%A9%E3%83%BC%E3%83%A1%E3%83%B3&ia=web")
        )
        assertEquals(
                "orange",
                searchQueryExtractor("https://search.yahoo.com/search?p=orange")
        )
        assertEquals(
                "Hamburger",
                searchQueryExtractor("https://www.yelp.com/search?find_desc=Hamburger&find_loc=Shinjuku%2C+%E6%9D%B1%E4%BA%AC%E9%83%BD%2C+Japan&ns=1")
        )
        assertEquals(
                "orange",
                searchQueryExtractor("https://www.flickr.com/search/?text=orange")
        )
        assertEquals(
                "orange",
                searchQueryExtractor("https://www.youtube.com/results?search_query=orange")
        )
        assertEquals(
                "orange",
                searchQueryExtractor("https://yandex.com/search/?text=orange&lr=10636")
        )
        assertEquals(
                "orange",
                searchQueryExtractor("https://www.baidu.com/s?wd=orange")
        )
        assertEquals(
                "orange",
                searchQueryExtractor("https://ja.wikipedia.org/wiki/orange")
        )
        assertEquals(
                "test",
                searchQueryExtractor("https://search.gmx.com/web?q=test&origin=HP_sf_atf")
        )
        assertEquals(
                "test",
                searchQueryExtractor("https://www.teoma.com/web?q=test&tpr=1&ts=1601594078977")
        )
        assertEquals(
                "test",
                searchQueryExtractor("https://www.info.com/serp?q=test&sc=ZkYgeSO4txSIw")
        )
        assertEquals(
                "test",
                searchQueryExtractor("https://results.looksmart.com/serp?q=test")
        )
        assertEquals(
                "test",
                searchQueryExtractor("https://www.privacywall.org/search/secure/?q=test")
        )
        assertEquals(
                "test",
                searchQueryExtractor("https://www.ecosia.org/search?q=test")
        )
        assertEquals(
                "test",
                searchQueryExtractor("https://nova.rambler.ru/search?query=test&utm_source=head&utm_campaign=self_promo&utm_medium=form&utm_content=search&_openstat=Umcl9N")
        )
    }

    @Test
    fun testElseCase() {
        assertEquals(
                "orange",
                searchQueryExtractor("https://www.sample-search.com/search?q=orange")
        )
    }

}