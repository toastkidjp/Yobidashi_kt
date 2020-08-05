package jp.toastkid.yobidashi.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class FragmentReplacingUseCaseTest {

    private lateinit var fragmentReplacingUseCase: FragmentReplacingUseCase

    @MockK
    private lateinit var supportFragmentManager: FragmentManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fragmentReplacingUseCase = FragmentReplacingUseCase(supportFragmentManager)
    }

    @Test
    fun testPassedSameFragment() {
        val fragment = mockk<Fragment>()
        every { supportFragmentManager.findFragmentById(any()) }.answers { fragment }
        every { supportFragmentManager.getFragments() }.answers { emptyList() }

        fragmentReplacingUseCase.invoke(fragment, false, false)

        verify(exactly = 1) { supportFragmentManager.findFragmentById(any()) }
        verify(exactly = 0) { supportFragmentManager.getFragments() }
    }

    @Test
    fun testTransaction() {
        every { supportFragmentManager.findFragmentById(any()) }.answers { mockk() }
        every { supportFragmentManager.getFragments() }.answers { emptyList() }

        val transaction = mockk<FragmentTransaction>()
        every { transaction.replace(any(), any<Fragment>(), any()) }.answers { transaction }
        every { transaction.addToBackStack(any()) }.answers { transaction }
        every { transaction.commitAllowingStateLoss() }.answers { 1 }
        every { supportFragmentManager.beginTransaction() }.answers { transaction }

        fragmentReplacingUseCase.invoke(mockk(), false, false)

        verify(exactly = 1) { supportFragmentManager.findFragmentById(any()) }
        verify(exactly = 1) { supportFragmentManager.getFragments() }
        verify(exactly = 1) { supportFragmentManager.beginTransaction() }
        verify(exactly = 1) { transaction.replace(any(), any<Fragment>(), any()) }
        verify(exactly = 1) { transaction.addToBackStack(any()) }
        verify(exactly = 1) { transaction.commitAllowingStateLoss() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}