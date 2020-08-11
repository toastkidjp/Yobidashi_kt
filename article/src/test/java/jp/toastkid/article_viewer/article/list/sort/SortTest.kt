package jp.toastkid.article_viewer.article.list.sort

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.article_viewer.article.ArticleRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class SortTest {

    @MockK
    private lateinit var repository: ArticleRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { repository.orderByLastModified() }.answers { mockk() }
        every { repository.orderByLength() }.answers { mockk() }
        every { repository.orderByName() }.answers { mockk() }
    }

    @Test
    fun testSortLastModified() {
        Sort.LAST_MODIFIED.invoke(repository)

        verify(exactly = 1) { repository.orderByLastModified() }
        verify(exactly = 0) { repository.orderByLength() }
        verify(exactly = 0) { repository.orderByName() }
    }

    @Test
    fun testSortLength() {
        Sort.LENGTH.invoke(repository)

        verify(exactly = 0) { repository.orderByLastModified() }
        verify(exactly = 1) { repository.orderByLength() }
        verify(exactly = 0) { repository.orderByName() }
    }

    @Test
    fun testSortName() {
        Sort.NAME.invoke(repository)

        verify(exactly = 0) { repository.orderByLastModified() }
        verify(exactly = 0) { repository.orderByLength() }
        verify(exactly = 1) { repository.orderByName() }
    }

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

    @After
    fun tearDown() {
        unmockkAll()
    }

}