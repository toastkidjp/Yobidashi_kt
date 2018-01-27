package jp.toastkid.yobidashi.search

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [SearchCategory]'s test.
 *
 * @author toastkidjp
 */
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
}