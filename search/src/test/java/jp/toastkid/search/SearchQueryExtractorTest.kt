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

    private val testPairs = mapOf(
            "かもめ" to "https://www.google.com/search?q=%E3%81%8B%E3%82%82%E3%82%81",
            "かもめ" to "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=%E3%81%8B%E3%82%82%E3%82%81",
            "programmer" to "https://www.job.com/search?query=programmer",
            "Noodle" to "https://commons.wikimedia.org/w/index.php?search=Noodle&ns0=1",
            "スウィープ" to "https://ja.wikipedia.org/wiki/%E3%82%B9%E3%82%A6%E3%82%A3%E3%83%BC%E3%83%97",
            "ラーメン" to "https://www.tumblr.com/search/%E3%83%A9%E3%83%BC%E3%83%A1%E3%83%B3",
            "java" to "https://www.reddit.com/search/?q=java",
            "kotlin" to "https://www.quora.com/search?q=kotlin",
            "train" to "https://archive.org/search.php?query=train",
            "tomato" to "https://search.naver.com/search.naver?ie=utf8&query=tomato",
            "orange" to "https://search.daum.net/search?w=tot&q=orange",
            "orange" to "https://www.wolframalpha.com/input/?i=orange",
            "Yahoo" to "https://www.linkedin.com/jobs/search?keywords=Yahoo",
            "Yahoo" to "https://www.ft.com/search?q=Yahoo",
            "Yahoo" to "https://web.archive.org/web/*/Yahoo",
            "tomato" to "https://www.qwant.com/?q=tomato",
            "tomato" to "https://www.startpage.com/sp/search?q=tomato",
            "poirot" to "https://www.imdb.com/find?q=poirot",
            "poirot" to "https://m.facebook.com/public/poirot",
            "Soccer" to "https://www.espn.com/search/_/q/Soccer",
            "ラーメン" to "https://duckduckgo.com/?q=%E3%83%A9%E3%83%BC%E3%83%A1%E3%83%B3&ia=web",
            "orange" to "https://search.yahoo.com/search?p=orange",
            "Hamburger" to "https://www.yelp.com/search?find_desc=Hamburger&find_loc=Shinjuku%2C+%E6%9D%B1%E4%BA%AC%E9%83%BD%2C+Japan&ns=1",
            "orange" to "https://www.flickr.com/search/?text=orange",
            "orange" to "https://www.youtube.com/results?search_query=orange",
            "orange" to "https://m.youtube.com/results?search_query=orange",
            "orange" to "https://yandex.com/search/?text=orange&lr=10636",
            "orange" to "https://www.baidu.com/s?wd=orange",
            "orange" to "https://ja.wikipedia.org/wiki/orange",
            "test" to "https://search.gmx.com/web?q=test&origin=HP_sf_atf",
            "test" to "https://www.teoma.com/web?q=test&tpr=1&ts=1601594078977",
            "test" to "https://www.info.com/serp?q=test&sc=ZkYgeSO4txSIw",
            "test" to "https://results.looksmart.com/serp?q=test",
            "test" to "https://www.mojeek.com/search?q=test",
            "test" to "https://www.privacywall.org/search/secure/?q=test",
            "test" to "https://www.ecosia.org/search?q=test",
            "test" to "https://nova.rambler.ru/search?query=test&utm_source=head&utm_campaign=self_promo&utm_medium=form&utm_content=search&_openstat=Umcl9N",
            "test" to "https://www.findx.com/search?q=test&p=1",
            "test" to "https://search.goo.ne.jp/web.jsp?MT=test",
            "test" to "https://www.bbc.co.uk/search?q=test",
            "test" to "https://bgr.com/?s=test#",
            "test" to "https://alohafind.com/search/?q=test#gsc.tab=0&gsc.q=test&gsc.page=1",
            "test" to "https://www.givero.com/search?q=test"
    )

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
        testPairs.forEach {
            assertEquals(it.key, searchQueryExtractor(it.value))
        }
    }

    @Test
    fun testElseCase() {
        assertEquals(
                "orange",
                searchQueryExtractor("https://www.sample-search.com/search?q=orange")
        )
    }

}