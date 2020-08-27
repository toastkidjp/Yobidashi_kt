package jp.toastkid.yobidashi.media.image.preview

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ImageEditChooserFactoryTest {

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setDataAndType(any(), any()) }.answers { mockk() }
        every { anyConstructed<Intent>().setFlags(any()) }.answers { mockk() }
        mockkStatic(Intent::class)
        every { Intent.createChooser(any(), any()) }.answers { mockk() }
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) }.answers { mockk() }

        ImageEditChooserFactory().invoke(context, "test/path")

        verify(exactly = 1) { anyConstructed<Intent>().setDataAndType(any(), any()) }
        verify(exactly = 1) { anyConstructed<Intent>().setFlags(any()) }
        verify(exactly = 1) { Intent.createChooser(any(), any()) }
        verify(exactly = 1) { FileProvider.getUriForFile(any(), any(), any()) }
    }

}