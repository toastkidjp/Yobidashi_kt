package jp.toastkid.lib.color

import android.graphics.Color
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test

/**
 * @author toastkidjp
 */
class LinkColorGeneratorTest {

    @Test
    fun test() {
        mockkStatic(Color::class)
        every { Color.rgb(any<Int>(), any(), any()) }.answers { 1 }
        every { Color.red(any()) }.answers { 1 }
        every { Color.blue(any()) }.answers { 1 }
        every { Color.green(any()) }.answers { 1 }

        LinkColorGenerator().invoke(Color.RED)

        verify (exactly = 1) { Color.rgb(any<Int>(), any(), any()) }
        verify (exactly = 1) { Color.red(any()) }
        verify (exactly = 1) { Color.blue(any()) }
        verify (exactly = 1) { Color.green(any()) }
    }
}