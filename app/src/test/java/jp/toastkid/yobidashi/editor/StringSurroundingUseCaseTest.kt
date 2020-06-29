package jp.toastkid.yobidashi.editor

import android.text.Editable
import android.widget.EditText
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * @author toastkidjp
 */
class StringSurroundingUseCaseTest {

    @Test
    fun testInvoke() {
        val useCase = StringSurroundingUseCase()

        val text = mockk<Editable>()
        every { text.subSequence(any(), any()) }.returns("tomato")
        every { text.replace(any(), any(), any<String>()) }.returns(mockk())

        val editText = mockk<EditText>()
        every { editText.text }.returns(text)
        every { editText.selectionStart }.returns(2)
        every { editText.selectionEnd }.returns(3)

        useCase.invoke(editText, '"')

        verify(atLeast = 1) { text.replace(2, 3, any<String>()) }
    }
}