package jp.toastkid.yobidashi.main

import androidx.fragment.app.FragmentManager
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.floating.FloatingPreview
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.menu.MenuViewModel
import jp.toastkid.yobidashi.tab.TabAdapter
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class OnBackPressedUseCaseTest {

    private lateinit var onBackPressedUseCase: OnBackPressedUseCase

    @MockK
    private lateinit var tabListUseCase: TabListUseCase

    @MockK
    private lateinit var menuVisibility: () -> Boolean

    @MockK
    private lateinit var menuViewModel: MenuViewModel

    @MockK
    private lateinit var pageSearcherModule: PageSearcherModule

    @MockK
    private lateinit var floatingPreview: FloatingPreview

    @MockK
    private lateinit var tabs: TabAdapter

    @MockK
    private lateinit var onEmptyTabs: () -> Unit

    @MockK
    private lateinit var replaceToCurrentTab: TabReplacingUseCase

    @MockK
    private lateinit var supportFragmentManager: FragmentManager
    
    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        onBackPressedUseCase = OnBackPressedUseCase(
                tabListUseCase,
                menuVisibility,
                menuViewModel,
                pageSearcherModule,
                { floatingPreview },
                tabs,
                onEmptyTabs,
                replaceToCurrentTab,
                supportFragmentManager
        )

        every { menuViewModel.close() }.just(Runs)
    }

    @Test
    fun testTabListUseCaseIsTrue() {
        every { tabListUseCase.onBackPressed() }.returns(true)
        every { menuVisibility.invoke() }.returns(true)

        onBackPressedUseCase.invoke()

        verify (exactly = 1) { tabListUseCase.onBackPressed() }
        verify (exactly = 0) { menuVisibility.invoke() }
        verify (exactly = 0) { menuViewModel.close() }
    }

    @Test
    fun testMenuIsVisible() {
        every { tabListUseCase.onBackPressed() }.returns(false)
        every { menuVisibility.invoke() }.returns(true)
        every { pageSearcherModule.isVisible() }.returns(false)

        onBackPressedUseCase.invoke()

        verify (exactly = 1) { tabListUseCase.onBackPressed() }
        verify (exactly = 1) { menuVisibility.invoke() }
        verify (exactly = 1) { menuViewModel.close() }
        verify (exactly = 0) { pageSearcherModule.isVisible() }
    }

    @Test
    fun testPageSearcherModuleIsVisible() {
        every { tabListUseCase.onBackPressed() }.returns(false)
        every { menuVisibility.invoke() }.returns(false)
        every { pageSearcherModule.isVisible() }.returns(true)
        every { pageSearcherModule.hide() }.just(Runs)
        every { floatingPreview.onBackPressed() }.answers { true }

        onBackPressedUseCase.invoke()

        verify (exactly = 1) { tabListUseCase.onBackPressed() }
        verify (exactly = 1) { menuVisibility.invoke() }
        verify (exactly = 0) { menuViewModel.close() }
        verify (exactly = 1) { pageSearcherModule.isVisible() }
        verify (exactly = 1) { pageSearcherModule.hide() }
        verify (exactly = 0) { floatingPreview.onBackPressed() }
    }

    @Test
    fun testFloatingPreviewIsVisible() {
        every { tabListUseCase.onBackPressed() }.returns(false)
        every { menuVisibility.invoke() }.returns(false)
        every { pageSearcherModule.isVisible() }.returns(false)
        every { pageSearcherModule.hide() }.answers { Unit }
        every { floatingPreview.onBackPressed() }.answers { false }
        every { floatingPreview.hide() }.answers { Unit }
        val fragment = mockk<BrowserFragment>()
        every { supportFragmentManager.findFragmentById(any()) }.answers { fragment }
        every { fragment.pressBack() }.returns(true)
        every { tabs.closeTab(any()) }.answers { Unit }

        onBackPressedUseCase.invoke()

        verify (exactly = 1) { tabListUseCase.onBackPressed() }
        verify (exactly = 1) { menuVisibility.invoke() }
        verify (exactly = 0) { menuViewModel.close() }
        verify (exactly = 1) { pageSearcherModule.isVisible() }
        verify (exactly = 0) { pageSearcherModule.hide() }
        verify (exactly = 1) { floatingPreview.onBackPressed() }
        verify (exactly = 0) { floatingPreview.hide() }
        verify (exactly = 1) { supportFragmentManager.findFragmentById(any()) }
        verify (exactly = 1) { fragment.pressBack() }
        verify (exactly = 0) { tabs.closeTab(any()) }
    }

    /*
        if (currentFragment is BrowserFragment
                || currentFragment is PdfViewerFragment
                || currentFragment is ContentViewerFragment
        ) {
            tabs.closeTab(tabs.index())

            if (tabs.isEmpty()) {
                onEmptyTabs()
                return
            }
            replaceToCurrentTab(true)
            return
        }

        val fragment = findFragment()
        if (fragment !is EditorFragment) {
            supportFragmentManager.popBackStackImmediate()
            return
        }

        confirmExit()
     */
    @After
    fun tearDown() {
        unmockkAll()
    }

}