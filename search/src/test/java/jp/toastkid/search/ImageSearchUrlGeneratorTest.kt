package jp.toastkid.search

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ImageSearchUrlGeneratorTest {

    private lateinit var imageSearchUrlGenerator: ImageSearchUrlGenerator

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.answers { mockk() }

        imageSearchUrlGenerator = ImageSearchUrlGenerator()
    }

    @Test
    fun test() {
        imageSearchUrlGenerator.invoke("https://www.yahoo.co.jp")

        verify(exactly = 1) { Uri.parse(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}