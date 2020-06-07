package jp.toastkid.yobidashi.libs

import android.graphics.Color
import android.widget.EditText
import androidx.core.graphics.ColorUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class EditTextColorSetterTest {

    @Test
    fun test() {
        val editText = mockk<EditText>()
        every { editText.setTextColor(any<Int>()) }.answers { nothing }
        every { editText.setHintTextColor(any<Int>()) }.answers { nothing }
        every { editText.highlightColor = any<Int>() }.answers { nothing }

        EditTextColorSetter().invoke(editText, Color.BLACK)

        verify(atLeast = 1) { editText.setTextColor(Color.BLACK) }
        verify(atLeast = 1) { editText.setHintTextColor(ColorUtils.setAlphaComponent(Color.BLACK, 196)) }
        verify(atLeast = 1) {
            editText.highlightColor = ColorUtils.setAlphaComponent(Color.BLACK, 128)
        }
    }

}