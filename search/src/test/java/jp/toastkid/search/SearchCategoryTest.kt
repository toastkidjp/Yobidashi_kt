package jp.toastkid.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * [SearchCategory]'s test.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class SearchCategoryTest {

    @Test
    fun test_getDefault() {
        assertEquals(SearchCategory.GOOGLE, SearchCategory.getDefault())
    }

    @Test
    fun test_getDefaultCategoryName() {
        assertEquals(SearchCategory.GOOGLE.name, SearchCategory.getDefaultCategoryName())
    }

    @Test
    fun test_findByCategory() {
        assertEquals(SearchCategory.GITHUB, SearchCategory.findByCategory("github"))
        assertEquals(SearchCategory.getDefault(), SearchCategory.findByCategory("none"))

    }

    @Test
    fun test_findIndex() {
        assertEquals(SearchCategory.GITHUB.ordinal, SearchCategory.findIndex("github"))
        assertEquals(SearchCategory.getDefault().ordinal, SearchCategory.findIndex("none"))
    }

    @Test
    fun testFindByHostOrNull() {
        assertNull(SearchCategory.findByUrlOrNull(null))
        assertNull(SearchCategory.findByUrlOrNull(""))
        assertNull(SearchCategory.findByUrlOrNull(" "))
        assertNull(SearchCategory.findByUrlOrNull("https://www.yahoo.co.jp"))
        assertSame(SearchCategory.GOOGLE, SearchCategory.findByUrlOrNull("https://www.google.com"))
        assertSame(
                SearchCategory.YAHOO_JAPAN_REALTIME_SEARCH,
                SearchCategory.findByUrlOrNull("https://search.yahoo.co.jp/realtime/search?p=test")
        )
        assertSame(SearchCategory.YAHOO_JAPAN, SearchCategory.findByUrlOrNull("https://search.yahoo.co.jp"))
        assertSame(SearchCategory.FLICKR, SearchCategory.findByUrlOrNull("https://www.flickr.com"))
        assertSame(SearchCategory.AOL, SearchCategory.findByUrlOrNull("https://www.aolsearch.com"))
        assertSame(SearchCategory.WOLFRAM_ALPHA, SearchCategory.findByUrlOrNull("https://www.wolframalpha.com"))
        assertSame(SearchCategory.LINKED_IN, SearchCategory.findByUrlOrNull("https://www.linkedin.com/jobs/search"))
    }

}