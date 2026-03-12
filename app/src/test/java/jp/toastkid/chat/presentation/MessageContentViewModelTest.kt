package jp.toastkid.chat.presentation

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class MessageContentViewModelTest {

    private lateinit var subject: MessageContentViewModel

    @Before
    fun setUp() {
        subject = MessageContentViewModel()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun loadImage() {
    }

    @Test
    fun showImage() {
    }

    @Test
    fun openImageDropdownMenu() {
    }

    @Test
    fun openingImageDropdownMenu() {
    }

    @Test
    fun closeImageDropdownMenu() {
    }


    @Test
    fun lineText() {
        assertEquals("- Text", subject.lineText(false, "- Text").text)
        assertEquals("Text", subject.lineText(true, "- Text").text)
    }

    @Test
    fun image() {
        val image = subject.image()

        val image2 = subject.image()

        assertSame(image, image2)
    }

}