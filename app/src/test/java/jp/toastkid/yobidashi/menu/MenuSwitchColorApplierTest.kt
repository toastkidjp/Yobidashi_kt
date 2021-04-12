package jp.toastkid.yobidashi.menu

import android.graphics.Color
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.ColorPair
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class MenuSwitchColorApplierTest {

    private lateinit var menuSwitchColorApplier: MenuSwitchColorApplier

    @MockK
    private lateinit var fab: FloatingActionButton

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        menuSwitchColorApplier = MenuSwitchColorApplier(fab)
    }

    @Test
    fun test() {
        val first: ColorPair = mockk()
        every { first.bgColor() }.answers { Color.BLACK }
        every { first.applyReverseTo(any()) }.answers { Unit }

        menuSwitchColorApplier.invoke(first)

        verify(exactly = 1) { first.bgColor() }
        verify(exactly = 1) { first.applyReverseTo(any()) }

        val second: ColorPair = mockk()
        every { second.bgColor() }.answers { Color.BLACK }
        every { second.applyReverseTo(any()) }.answers { Unit }

        menuSwitchColorApplier.invoke(second)

        verify(exactly = 1) { second.bgColor() }
        verify(exactly = 0) { second.applyReverseTo(any()) }

        val third: ColorPair = mockk()
        every { third.bgColor() }.answers { Color.WHITE }
        every { third.applyReverseTo(any()) }.answers { Unit }

        menuSwitchColorApplier.invoke(third)

        verify(exactly = 1) { third.bgColor() }
        verify(exactly = 1) { third.applyReverseTo(any()) }    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}