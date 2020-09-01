package jp.toastkid.yobidashi.main

import android.content.res.Resources
import android.view.View
import android.view.ViewPropertyAnimator
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.ScreenMode
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class AppBarVisibilityUseCaseTest {

    private lateinit var appBarVisibilityUseCase: AppBarVisibilityUseCase

    @MockK
    private lateinit var appBar: View

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var animator: ViewPropertyAnimator

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { appBar.getResources() }.answers { resources }
        every { appBar.setVisibility(any()) }.answers { Unit }
        every { appBar.animate() }.answers { animator }
        every { animator.cancel() }.answers { Unit }
        every { animator.translationY(any()) }.answers { animator }
        every { animator.setDuration(any()) }.answers { animator }
        every { animator.withStartAction(any()) }.answers { animator }
        every { animator.withEndAction(any()) }.answers { animator }
        every { animator.start() }.answers { animator }

        appBarVisibilityUseCase = AppBarVisibilityUseCase(appBar, preferenceApplier)
    }

    @Test
    fun testShowFullScreen() {
        every { preferenceApplier.browserScreenMode() }.answers { ScreenMode.FULL_SCREEN.name }

        appBarVisibilityUseCase.show()

        verify(exactly = 1) { appBar.getResources() }
        verify(exactly = 0) { appBar.animate() }
    }

    @Test
    fun testShowFixed() {
        every { preferenceApplier.browserScreenMode() }.answers { ScreenMode.FIXED.name }
        every { appBar.setVisibility(any()) }.answers { Unit }

        appBarVisibilityUseCase.show()

        verify(exactly = 1) { appBar.getResources() }
        verify(exactly = 1) { appBar.setVisibility(View.VISIBLE) }
        verify(exactly = 0) { appBar.animate() }
    }

    @Test
    fun testShowExpandable() {
        every { preferenceApplier.browserScreenMode() }.answers { ScreenMode.EXPANDABLE.name }

        appBarVisibilityUseCase.show()

        verify(exactly = 1) { appBar.getResources() }
        verify(exactly = 0) { appBar.setVisibility(View.VISIBLE) }
        verify(exactly = 1) { appBar.animate() }
    }

    @Test
    fun hideFixed() {
        every { preferenceApplier.browserScreenMode() }.answers { ScreenMode.FIXED.name }

        appBarVisibilityUseCase.hide()

        verify(exactly = 1) { appBar.getResources() }
        verify(exactly = 0) { appBar.setVisibility(any()) }
        verify(exactly = 0) { appBar.animate() }
    }

    @Test
    fun hideFullScreen() {
        every { preferenceApplier.browserScreenMode() }.answers { ScreenMode.FULL_SCREEN.name }

        appBarVisibilityUseCase.hide()

        verify(exactly = 1) { appBar.getResources() }
        verify(exactly = 1) { appBar.setVisibility(View.GONE) }
        verify(exactly = 0) { appBar.animate() }
    }

    @Test
    fun hideExpandable() {
        every { preferenceApplier.browserScreenMode() }.answers { ScreenMode.EXPANDABLE.name }
        every { resources.getDimension(any()) }.answers { 1f }

        appBarVisibilityUseCase.hide()

        verify(exactly = 1) { appBar.getResources() }
        verify(exactly = 0) { appBar.setVisibility(View.GONE) }
        verify(exactly = 1) { appBar.animate() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}