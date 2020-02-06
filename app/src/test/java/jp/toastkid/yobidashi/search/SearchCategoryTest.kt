package jp.toastkid.yobidashi.search

import org.junit.Assert.*
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
        assertEquals("18", "${SearchCategory.findIndex("github")}")
        assertEquals("0", "${SearchCategory.findIndex("none")}")
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
    }

}