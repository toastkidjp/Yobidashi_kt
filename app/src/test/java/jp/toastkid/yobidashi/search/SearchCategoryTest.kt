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
    }

    @Test
    fun test_findIndex() {
        assertEquals("18", "${SearchCategory.findIndex("github")}")
    }
}