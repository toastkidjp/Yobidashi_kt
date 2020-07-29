package jp.toastkid.yobidashi.media.image.preview

import android.content.ActivityNotFoundException
import android.content.Intent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class EditorChooserInvokingUseCaseTest {

    @MockK
    private lateinit var pathFinder: () -> String?

    @MockK
    private lateinit var showErrorMessage: () -> Unit

    @MockK
    private lateinit var activityStarter: (Intent) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testPathNotFound() {
        every { pathFinder.invoke() }.returns(null)
        every { showErrorMessage.invoke() }.answers { Unit }
        every { activityStarter.invoke(any()) }.answers { Unit }

        mockkConstructor(ImageEditChooserFactory::class)
        every { anyConstructed<ImageEditChooserFactory>().invoke(any(), any()) }.answers { mockk() }

        EditorChooserInvokingUseCase(
                pathFinder,
                showErrorMessage,
                activityStarter
        ).invoke(mockk())

        verify(exactly = 1) { pathFinder.invoke() }
        verify(exactly = 1) { showErrorMessage.invoke() }
        verify(exactly = 0) { anyConstructed<ImageEditChooserFactory>().invoke(any(), any()) }
        verify(exactly = 0) { activityStarter.invoke(any()) }
    }

    @Test
    fun testInvocationFailed() {
        every { pathFinder.invoke() }.returns("test")
        every { showErrorMessage.invoke() }.answers { Unit }
        every { activityStarter.invoke(any()) }.throws(mockk<ActivityNotFoundException>())

        mockkConstructor(ImageEditChooserFactory::class)
        every { anyConstructed<ImageEditChooserFactory>().invoke(any(), any()) }.answers { mockk() }

        EditorChooserInvokingUseCase(
                pathFinder,
                showErrorMessage,
                activityStarter
        ).invoke(mockk())

        verify(exactly = 1) { pathFinder.invoke() }
        verify(exactly = 1) { showErrorMessage.invoke() }
        verify(exactly = 1) { anyConstructed<ImageEditChooserFactory>().invoke(any(), any()) }
        verify(exactly = 1) { activityStarter.invoke(any()) }
    }

    @Test
    fun testSuccessful() {
        every { pathFinder.invoke() }.returns("test")
        every { showErrorMessage.invoke() }.answers { Unit }
        every { activityStarter.invoke(any()) }.answers { Unit }

        mockkConstructor(ImageEditChooserFactory::class)
        every { anyConstructed<ImageEditChooserFactory>().invoke(any(), any()) }.answers { mockk() }

        EditorChooserInvokingUseCase(
                pathFinder,
                showErrorMessage,
                activityStarter
        ).invoke(mockk())

        verify(exactly = 1) { pathFinder.invoke() }
        verify(exactly = 0) { showErrorMessage.invoke() }
        verify(exactly = 1) { anyConstructed<ImageEditChooserFactory>().invoke(any(), any()) }
        verify(exactly = 1) { activityStarter.invoke(any()) }
    }

}