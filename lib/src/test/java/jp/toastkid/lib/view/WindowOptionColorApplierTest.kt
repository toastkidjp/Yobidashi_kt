package jp.toastkid.lib.view

import android.graphics.Color
import android.view.Window
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.ColorPair
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class WindowOptionColorApplierTest {

    private lateinit var windowOptionColorApplier: WindowOptionColorApplier

    @MockK
    private lateinit var window: Window

    @Before
    fun setUp() {
        windowOptionColorApplier = WindowOptionColorApplier()
        MockKAnnotations.init(this)
        every { window.setStatusBarColor(any()) }.answers { Unit }
        every { window.setNavigationBarColor(any()) }.answers { Unit }
    }

    @Test
    fun test() {
        windowOptionColorApplier.invoke(window, ColorPair(Color.BLACK, Color.WHITE))

        verify(exactly = 1) { window.setStatusBarColor(any()) }
        verify(exactly = 1) { window.setNavigationBarColor(any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}