package jp.toastkid.yobidashi.search

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
        assertNull(SearchCategory.findByHostOrNull(null))
        assertNull(SearchCategory.findByHostOrNull(""))
        assertNull(SearchCategory.findByHostOrNull(" "))
        assertNull(SearchCategory.findByHostOrNull("www.yahoo.co.jp"))
        assertSame(SearchCategory.GOOGLE, SearchCategory.findByHostOrNull("www.google.com"))
        assertSame(SearchCategory.YAHOO_JAPAN, SearchCategory.findByHostOrNull("search.yahoo.co.jp"))
        assertSame(SearchCategory.FLICKR, SearchCategory.findByHostOrNull("www.flickr.com"))
        assertSame(SearchCategory.AOL, SearchCategory.findByHostOrNull("www.aolsearch.com"))
        assertSame(SearchCategory.WOLFRAM_ALPHA, SearchCategory.findByHostOrNull("www.wolframalpha.com"))
        assertSame(SearchCategory.LINKED_IN, SearchCategory.findByHostOrNull("www.linkedin.com"))
    }

}