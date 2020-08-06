package jp.toastkid.yobidashi.gesture

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.storage.ExternalFileAssignment
import jp.toastkid.yobidashi.libs.BitmapCompressor
import jp.toastkid.yobidashi.libs.ThumbnailGenerator
import org.junit.After
import org.junit.Test
import java.io.File

/**
 * @author toastkidjp
 */
class CanvasStoreScenarioTest {

    @Test
    fun testThumbnailIsNull() {
        mockkConstructor(ThumbnailGenerator::class)
        every { anyConstructed<ThumbnailGenerator>().invoke(any()) }.answers { null }
        mockkConstructor(ExternalFileAssignment::class)
        every { anyConstructed<ExternalFileAssignment>().invoke(any(), any()) }.answers { mockk() }

        CanvasStoreScenario().invoke(mockk(), mockk())

        verify(exactly = 1) { anyConstructed<ThumbnailGenerator>().invoke(any()) }
        verify(exactly = 0) { anyConstructed<ExternalFileAssignment>().invoke(any(), any()) }
    }

    @Test
    fun testThumbnail() {
        mockkConstructor(ThumbnailGenerator::class)
        every { anyConstructed<ThumbnailGenerator>().invoke(any()) }.answers { mockk() }

        val file = mockk<File>()
        every { file.getAbsolutePath() }.answers { "test" }
        mockkConstructor(ExternalFileAssignment::class)
        every { anyConstructed<ExternalFileAssignment>().invoke(any(), any()) }.answers { file }
        mockkConstructor(BitmapCompressor::class)
        every { anyConstructed<BitmapCompressor>().invoke(any(), any()) }.answers { Unit }
        val contentViewModel = mockk<ContentViewModel>()
        every { contentViewModel.snackShort(any<String>()) }.answers { Unit }
        mockkConstructor(ViewModelProvider::class)
        every { anyConstructed<ViewModelProvider>().get(any<Class<out ViewModel>>()) }.answers { contentViewModel }
        val activity = mockk<FragmentActivity>()
        every { activity.getViewModelStore() }.answers { mockk() }
        every { activity.getDefaultViewModelProviderFactory() }.answers { mockk() }
        every { activity.getString(any(), any()) }.answers { "test" }

        CanvasStoreScenario().invoke(activity, mockk())

        verify(exactly = 1) { anyConstructed<ThumbnailGenerator>().invoke(any()) }
        verify(exactly = 1) { anyConstructed<ExternalFileAssignment>().invoke(any(), any()) }
        verify(exactly = 1) { anyConstructed<BitmapCompressor>().invoke(any(), any()) }
        verify(exactly = 1) { contentViewModel.snackShort(any<String>()) }
        verify(exactly = 1) { anyConstructed<ViewModelProvider>().get(any<Class<out ViewModel>>()) }
        verify(exactly = 1) { activity.getViewModelStore() }
        verify(exactly = 1) { activity.getDefaultViewModelProviderFactory() }
        verify(exactly = 1) { activity.getString(any(), any()) }
        verify(exactly = 1) { file.getAbsolutePath() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}