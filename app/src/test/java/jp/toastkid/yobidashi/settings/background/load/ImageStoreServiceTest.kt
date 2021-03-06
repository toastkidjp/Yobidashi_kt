package jp.toastkid.yobidashi.settings.background.load

import android.graphics.Bitmap
import android.net.Uri
import android.view.Display
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.yobidashi.libs.BitmapScaling
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

/**
 * @author toastkidjp
 */
class ImageStoreServiceTest {

    @InjectMockKs
    private lateinit var imageStoreService: ImageStoreService

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

    @MockK
    private lateinit var bitmapScaling: BitmapScaling

    private lateinit var file: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        file = spyk(File.createTempFile("test", "webp"))
        every { filesDir.assignNewFile(any<String>()) }.answers { file }
        every { preferenceApplier.backgroundImagePath = any() }.answers { Unit }
        every { display.getRectSize(any()) }.answers { Unit }

        mockkConstructor(FileOutputStream::class)
        every { anyConstructed<FileOutputStream>().close() }.answers { Unit }

        every { scaledBitmap.compress(any(), any(), any()) }.answers { true }

        every { bitmapScaling.invoke(any(), any(), any()) }.answers { scaledBitmap }

        every { uri.getLastPathSegment() }.returns("last")
    }

    @After
    fun tearDown() {
        unmockkAll()
        file.delete()
    }

    @Test
    fun test() {
        imageStoreService.invoke(bitmap, uri, display)

        verify(exactly = 2) { file.getPath() }
        verify(exactly = 1) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 1) { display.getRectSize(any()) }
        verify(exactly = 1) { anyConstructed<FileOutputStream>().close() }
        verify(exactly = 1) { bitmapScaling.invoke(any(), any(), any()) }
        verify(exactly = 1) { uri.getLastPathSegment() }
    }

}