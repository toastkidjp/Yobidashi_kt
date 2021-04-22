package jp.toastkid.yobidashi.main

import androidx.fragment.app.FragmentManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class TabListUseCaseTest {

    @MockK
    private lateinit var fragmentManager: FragmentManager

    @MockK
    private lateinit var thumbnailRefresher: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { thumbnailRefresher.invoke() }.answers { Unit }
    }

    @Test
    fun testSwitchIsVisible() {
        mockkConstructor(TabListDialogFragment::class)
        every { anyConstructed<TabListDialogFragment>().isVisible() }.answers { true }
        every { anyConstructed<TabListDialogFragment>().dismiss() }.answers { Unit }

        TabListUseCase(fragmentManager, thumbnailRefresher).switch()

        verify(exactly = 1) { anyConstructed<TabListDialogFragment>().isVisible }
        verify(exactly = 1) { anyConstructed<TabListDialogFragment>().dismiss() }
        verify(exactly = 0) { thumbnailRefresher.invoke() }
    }

    @Test
    fun testSwitchIsNotVisible() {
        mockkConstructor(TabListDialogFragment::class)
        every { anyConstructed<TabListDialogFragment>().isVisible() }.answers { false }
        every { anyConstructed<TabListDialogFragment>().dismiss() }.answers { Unit }
        every { anyConstructed<TabListDialogFragment>().show(any<FragmentManager>(), any()) }.answers { Unit }

        TabListUseCase(fragmentManager, thumbnailRefresher).switch()

        verify(exactly = 1) { anyConstructed<TabListDialogFragment>().isVisible }
        verify(exactly = 0) { anyConstructed<TabListDialogFragment>().dismiss() }
        verify(exactly = 1) { thumbnailRefresher.invoke() }
        verify(exactly = 1) { anyConstructed<TabListDialogFragment>().show(any<FragmentManager>(), any()) }
    }

    @Test
    fun testOnBackPressedIsVisible() {
        mockkConstructor(TabListDialogFragment::class)
        every { anyConstructed<TabListDialogFragment>().isVisible() }.answers { true }
        every { anyConstructed<TabListDialogFragment>().dismiss() }.answers { Unit }

        assertTrue(TabListUseCase(fragmentManager, thumbnailRefresher).onBackPressed())

        verify(exactly = 1) { anyConstructed<TabListDialogFragment>().isVisible }
        verify(exactly = 1) { anyConstructed<TabListDialogFragment>().dismiss() }
    }

    @Test
    fun testOnBackPressedIsNotVisible() {
        mockkConstructor(TabListDialogFragment::class)
        every { anyConstructed<TabListDialogFragment>().isVisible() }.answers { false }
        every { anyConstructed<TabListDialogFragment>().dismiss() }.answers { Unit }

        assertFalse(TabListUseCase(fragmentManager, thumbnailRefresher).onBackPressed())

        verify(exactly = 1) { anyConstructed<TabListDialogFragment>().isVisible }
        verify(exactly = 0) { anyConstructed<TabListDialogFragment>().dismiss() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}