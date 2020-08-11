package jp.toastkid.article_viewer.article.list.sort

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * @author toastkidjp
 */
class SortTest {

    @Test
    fun testTitles() {
        val values = Sort.values()
        Sort.titles().forEachIndexed { index, s -> assertEquals(values[index].name, s) }
    }

    @Test
    fun testFindCurrentIndex() {
        assertEquals(0, Sort.findCurrentIndex(""))
        assertEquals(0, Sort.findCurrentIndex("orange"))
        assertEquals(0, Sort.findCurrentIndex("name"))
        assertEquals(1, Sort.findCurrentIndex("NAME"))
    }

    @Test
    fun testFindByName() {
        assertSame(Sort.LAST_MODIFIED, Sort.findByName(null))
        assertSame(Sort.LAST_MODIFIED, Sort.findByName(""))
        assertSame(Sort.NAME, Sort.findByName("name"))
        assertSame(Sort.LENGTH, Sort.findByName("LENGTH"))
    }

}