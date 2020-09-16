package jp.toastkid.yobidashi.main

import androidx.fragment.app.Fragment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jp.toastkid.yobidashi.browser.BrowserFragmentViewModel
import jp.toastkid.yobidashi.tab.TabAdapter
import kotlinx.coroutines.Job
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class TabReplacingUseCaseTest {

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

    @MockK
    private lateinit var disposables: Job

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testElseCase() {
        every { tabs.currentTab() }.answers { mockk() }
        every { tabs.saveTabList() }.answers { Unit }

        TabReplacingUseCase(
                tabs, obtainFragment, replaceFragment, browserFragmentViewModel, refreshThumbnail, runOnUiThread, disposables
        ).invoke(false)

        verify(exactly = 1) { tabs.currentTab() }
        verify(exactly = 1) { tabs.saveTabList() }
    }
}