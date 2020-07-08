package jp.toastkid.yobidashi.editor

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ContentHolderServiceTest {

    private lateinit var contentHolderService: ContentHolderService

    @Before
    fun setUp() {
        contentHolderService = ContentHolderService()
    }

    @Test
    fun test() {
        assertTrue(contentHolderService.isBlank())

        contentHolderService.setContent("tomato")
        assertFalse(contentHolderService.isBlank())
        assertEquals("tomato", contentHolderService.getContent())
    }
}