package jp.toastkid.yobidashi.editor

import android.text.Editable
import android.widget.EditText
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ListHeadAdderTest {

    private lateinit var listHeadAdder: ListHeadAdder

    @Before
    fun setUp() {
        listHeadAdder = ListHeadAdder()
    }

    @Test
    fun invoke() {
        val text = mockk<Editable>()
        every { text.subSequence(any(), any()) }.returns("tomato")
        every { text.replace(any(), any(), any<String>()) }.returns(mockk())

        val editText = mockk<EditText>()
        every { editText.text }.returns(text)
        every { editText.selectionStart }.returns(2)
        every { editText.selectionEnd }.returns(3)

        listHeadAdder.invoke(editText, "1.")

        verify(atLeast = 1) { text.replace(2, 3, any<String>()) }
    }
}