package jp.toastkid.image.preview.viewmodel

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import jp.toastkid.image.Image
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test

class ImagePreviewViewModelTest {

    private lateinit var subject: ImagePreviewViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        subject = ImagePreviewViewModel(0)

        subject.replaceImages(
            listOf(
                Image("/path/to/image1.webp", "image1.webp"),
                Image("/path/to/image2.webp", "image2.webp"),
            )
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun pagerState() {
        assertEquals(0, subject.pagerState().currentPage)
    }

    @Test
    fun sharedElementKey() {
        assertEquals("image_/path/to/image1.webp", subject.sharedElementKey(0))
        assertEquals("image_/path/to/image2.webp_", subject.sharedElementKey(1))
    }

    @Test
    fun getCurrentImage() {
        assertSame(subject.getImage(0), subject.getCurrentImage())
    }

    @Test
    fun getImage() {
        assertEquals(Image.makeEmpty(), subject.getImage(99))
    }

    @Test
    fun scale() {
        assertEquals(1.0f, subject.scale(0))
        assertEquals(1.0f, subject.scale(1))
    }

}
