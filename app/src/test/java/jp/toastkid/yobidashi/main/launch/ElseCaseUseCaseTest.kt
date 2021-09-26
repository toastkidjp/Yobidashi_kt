package jp.toastkid.yobidashi.main.launch

import androidx.fragment.app.Fragment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.browser.BrowserFragment
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ElseCaseUseCaseTest {

    private lateinit var elseCaseUseCase: ElseCaseUseCase

    @MockK
    private lateinit var tabIsEmpty: () -> Boolean

    @MockK
    private lateinit var openNewTab: () -> Unit

    @MockK
    private lateinit var findCurrentFragment: () -> Fragment?

    @MockK
    private lateinit var replaceToCurrentTab: (Boolean) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { openNewTab.invoke() }.answers { Unit }
        every { replaceToCurrentTab.invoke(any()) }.answers { Unit }

        elseCaseUseCase = ElseCaseUseCase(
                tabIsEmpty, openNewTab, findCurrentFragment, replaceToCurrentTab
        )
    }

    @Test
    fun testTabIsEmpty() {
        every { tabIsEmpty.invoke() }.answers { true }
        every { findCurrentFragment.invoke() }.answers { mockk() }

        elseCaseUseCase.invoke()

        verify(exactly = 1) { tabIsEmpty.invoke() }
        verify(exactly = 0) { findCurrentFragment.invoke() }
        verify(exactly = 0) { replaceToCurrentTab.invoke(false) }
    }

    @Test
    fun testTabIsNotEmptyAndCurrentIsNull() {
        every { tabIsEmpty.invoke() }.answers { false }
        every { findCurrentFragment.invoke() }.answers { null }

        elseCaseUseCase.invoke()

        verify(exactly = 1) { tabIsEmpty.invoke() }
        verify(exactly = 1) { findCurrentFragment.invoke() }
        verify(exactly = 1) { replaceToCurrentTab.invoke(false) }
    }

    @Test
    fun testTabIsNotEmptyAndCurrentIsTabUiFragment() {
        every { tabIsEmpty.invoke() }.answers { false }
        val fragment = mockk<BrowserFragment>()
        every { findCurrentFragment.invoke() }.answers { fragment }

        elseCaseUseCase.invoke()

        verify(exactly = 1) { tabIsEmpty.invoke() }
        verify(exactly = 1) { findCurrentFragment.invoke() }
        verify(exactly = 1) { replaceToCurrentTab.invoke(false) }
    }

    @Test
    fun testTabIsNotEmptyAndCurrentIsOtherFragment() {
        every { tabIsEmpty.invoke() }.answers { false }
        val fragment = mockk<Fragment>()
        every { findCurrentFragment.invoke() }.answers { fragment }

        elseCaseUseCase.invoke()

        verify(exactly = 1) { tabIsEmpty.invoke() }
        verify(exactly = 1) { findCurrentFragment.invoke() }
        verify(exactly = 0) { replaceToCurrentTab.invoke(false) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}