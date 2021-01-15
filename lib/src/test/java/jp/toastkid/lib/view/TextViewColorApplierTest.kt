package jp.toastkid.lib.view

import android.graphics.Color
import android.widget.TextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * @author toastkidjp
 */
class TextViewColorApplierTest {

    private val textViewColorApplier = TextViewColorApplier()

    @Test
    fun testInvokeWithNull() {
        textViewColorApplier(Color.WHITE, null)
    }

    @Test
    fun testInvoke() {
        val textView = mockk<TextView>()
        every { textView.setTextColor(any<Int>()) }.answers { Unit }
        every { textView.getCompoundDrawables() }.answers { arrayOf() }

        textViewColorApplier(Color.WHITE, textView)

        verify(atLeast = 1) { textView.setTextColor(Color.WHITE) }
        verify(atLeast = 1) { textView.getCompoundDrawables() }
    }
}