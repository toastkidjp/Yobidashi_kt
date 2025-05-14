package jp.toastkid.lib.intent

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.image.ImageCache
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class BitmapShareIntentFactoryTest {

    private lateinit var subject: BitmapShareIntentFactory

    @MockK
    private lateinit var imageCache: ImageCache

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var bitmap: Bitmap

    @MockK
    private lateinit var file: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.getCacheDir() }.returns(mockk())
        every { context.packageName }.returns("jp.toastkid.yobidashi.test")
        every { imageCache.saveBitmap(any(), any()) } returns file
        every { file.absoluteFile } returns mockk()

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) }.returns(mockk())

        subject = BitmapShareIntentFactory(imageCache)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        subject(context, bitmap)

        verify(exactly = 1) { context.getCacheDir() }
        verify(exactly = 1) { FileProvider.getUriForFile(any(), any(), any()) }
        verify(exactly = 1) { imageCache.saveBitmap(any(), any()) }
        verify(exactly = 1) { file.absoluteFile }
    }

}