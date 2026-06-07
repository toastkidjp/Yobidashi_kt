package jp.toastkid.markdown.presentation

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MarkdownPreviewViewModelTest {

    private lateinit var subject: MarkdownPreviewViewModel

    @Before
    fun setUp() {
        subject = MarkdownPreviewViewModel()
    }

    @Test
    fun extractText() {
        assertEquals("test", subject.extractText("test", false))
        assertEquals("test", subject.extractText("test", true))
        assertEquals("- test", subject.extractText("- test", false))
        assertEquals("test", subject.extractText("- [ ] test", true))
        assertEquals("", subject.extractText("- [ ] ", true))
        assertEquals("_", subject.extractText("] _", true))
        assertEquals("- [ ]", subject.extractText("- [ ]", true))
    }

    @Test
    fun makeFontWeight() {
        assertEquals(FontWeight.Normal, subject.makeFontWeight(-1))
        assertEquals(FontWeight.Bold, subject.makeFontWeight(1))
    }

    @Test
    fun makeTopMargin() {
        assertEquals(12, subject.makeTopMargin(1))
        assertEquals(12, subject.makeTopMargin(2))
        assertEquals(8, subject.makeTopMargin(3))
        assertEquals(4, subject.makeTopMargin(4))
        assertEquals(0, subject.makeTopMargin(5))
    }

}