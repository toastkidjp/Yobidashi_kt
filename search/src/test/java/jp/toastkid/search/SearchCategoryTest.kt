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
        assertEquals(jp.toastkid.search.SearchCategory.GOOGLE, jp.toastkid.search.SearchCategory.getDefault())
    }

    @Test
    fun test_getDefaultCategoryName() {
        assertEquals(jp.toastkid.search.SearchCategory.GOOGLE.name, jp.toastkid.search.SearchCategory.getDefaultCategoryName())
    }

    @Test
    fun test_findByCategory() {
        assertEquals(jp.toastkid.search.SearchCategory.GITHUB, jp.toastkid.search.SearchCategory.findByCategory("github"))
        assertEquals(jp.toastkid.search.SearchCategory.getDefault(), jp.toastkid.search.SearchCategory.findByCategory("none"))

    }

    @Test
    fun test_findIndex() {
        assertEquals(jp.toastkid.search.SearchCategory.GITHUB.ordinal, jp.toastkid.search.SearchCategory.findIndex("github"))
        assertEquals(jp.toastkid.search.SearchCategory.getDefault().ordinal, jp.toastkid.search.SearchCategory.findIndex("none"))
    }

    @Test
    fun testFindByHostOrNull() {
        assertNull(jp.toastkid.search.SearchCategory.findByHostOrNull(null))
        assertNull(jp.toastkid.search.SearchCategory.findByHostOrNull(""))
        assertNull(jp.toastkid.search.SearchCategory.findByHostOrNull(" "))
        assertNull(jp.toastkid.search.SearchCategory.findByHostOrNull("www.yahoo.co.jp"))
        assertSame(jp.toastkid.search.SearchCategory.GOOGLE, jp.toastkid.search.SearchCategory.findByHostOrNull("www.google.com"))
        assertSame(jp.toastkid.search.SearchCategory.YAHOO_JAPAN, jp.toastkid.search.SearchCategory.findByHostOrNull("search.yahoo.co.jp"))
        assertSame(jp.toastkid.search.SearchCategory.FLICKR, jp.toastkid.search.SearchCategory.findByHostOrNull("www.flickr.com"))
        assertSame(jp.toastkid.search.SearchCategory.AOL, jp.toastkid.search.SearchCategory.findByHostOrNull("www.aolsearch.com"))
        assertSame(jp.toastkid.search.SearchCategory.WOLFRAM_ALPHA, jp.toastkid.search.SearchCategory.findByHostOrNull("www.wolframalpha.com"))
        assertSame(jp.toastkid.search.SearchCategory.LINKED_IN, jp.toastkid.search.SearchCategory.findByHostOrNull("www.linkedin.com"))
    }

}