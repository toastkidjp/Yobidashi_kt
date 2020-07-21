package jp.toastkid.yobidashi.settings.background.load

import android.graphics.Bitmap
import android.net.Uri
import android.view.Display
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.yobidashi.libs.BitmapScaling
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

/**
 * @author toastkidjp
 */
class ImageStoreServiceTest {
    @MockK
    private lateinit var filesDir: FilesDir

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var bitmap: Bitmap

    @MockK
    private lateinit var uri: Uri

    @MockK
    private lateinit var display: Display

    @MockK
    private lateinit var scaledBitmap: Bitmap

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        val file = mockk<File>()
        every { file.getPath() }.answers { "test" }
        every { file["isInvalid"]() }.answers { false }
        every { filesDir.assignNewFile(any<Uri>()) }.answers { file }
        every { preferenceApplier.backgroundImagePath = any() }.answers { Unit }
        every { display.getRectSize(any()) }.answers { Unit }

        mockkConstructor(FileOutputStream::class)
        every { anyConstructed<FileOutputStream>().close() }.answers { Unit }

        every { scaledBitmap.compress(any(), any(), any()) }.answers { true }

        mockkObject(BitmapScaling)
        every { BitmapScaling.invoke(any(), any(), any()) }.answers { scaledBitmap }

        ImageStoreService(filesDir, preferenceApplier).invoke(bitmap, uri, display)

        verify(exactly = 2) { file.getPath() }
        verify(exactly = 1) { filesDir.assignNewFile(any<Uri>()) }
        verify(exactly = 1) { display.getRectSize(any()) }
        verify(exactly = 1) { anyConstructed<FileOutputStream>().close() }
        verify(exactly = 1) { BitmapScaling.invoke(any(), any(), any()) }
    }

}