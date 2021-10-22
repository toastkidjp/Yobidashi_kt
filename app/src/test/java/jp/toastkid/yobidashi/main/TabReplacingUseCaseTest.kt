package jp.toastkid.yobidashi.main

import androidx.fragment.app.Fragment
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jp.toastkid.yobidashi.browser.BrowserFragmentViewModel
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.ArticleListTab
import jp.toastkid.yobidashi.tab.model.CalendarTab
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class TabReplacingUseCaseTest {

    @InjectMockKs
    private lateinit var tabReplacingUseCase: TabReplacingUseCase

    @MockK
    private lateinit var tabs: TabAdapter

    @MockK
    private lateinit var obtainFragment: (Class<out Fragment>) -> Fragment

    @MockK
    private lateinit var replaceFragment: (Fragment, Boolean) -> Unit

    @MockK
    private lateinit var browserFragmentViewModel: BrowserFragmentViewModel

    @MockK
    private lateinit var refreshThumbnail: () -> Unit

    @MockK
    private lateinit var runOnUiThread: (() -> Unit) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { tabs.currentTab() }.answers { mockk() }
        every { tabs.saveTabList() }.just(Runs)
        every { replaceFragment(any(), any()) }.just(Runs)
        every { obtainFragment(any()) }.returns(mockk())
        every { runOnUiThread(any()) }.just(Runs)
        every { refreshThumbnail() }.returns(mockk())
    }

    @Test
    fun testElseCase() {
        tabReplacingUseCase.invoke(false)

        verify(exactly = 1) { tabs.currentTab() }
        verify(exactly = 1) { tabs.saveTabList() }
    }

    @Test
    fun testCalendarTabCase() {
        every { tabs.currentTab() }.answers { mockk<CalendarTab>() }

        tabReplacingUseCase.invoke(false)

        verify(exactly = 1) { tabs.currentTab() }
        verify(exactly = 1) { tabs.saveTabList() }
        verify(exactly = 1) { obtainFragment(any()) }
        verify(exactly = 1) { replaceFragment(any(), any()) }
    }

    @Test
    fun test() {
        every { tabs.currentTab() }.answers { mockk<ArticleListTab>() }

        tabReplacingUseCase.invoke(false)

        verify(exactly = 1) { tabs.currentTab() }
        verify(exactly = 1) { tabs.saveTabList() }
        verify(exactly = 1) { obtainFragment(any()) }
        verify(exactly = 1) { replaceFragment(any(), any()) }
    }

}