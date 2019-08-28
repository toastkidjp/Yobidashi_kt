package jp.toastkid.yobidashi.editor

import android.text.Editable
import android.widget.EditText
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author toastkidjp
 */
class SelectedTextExtractorTest {

    @Test
    fun test() {
        val editText = mockk<EditText>()

        every { editText.selectionStart }.returns(3)
        every { editText.selectionEnd }.returns(5)
        val editable = mockk<Editable>().also {
                    every { it.toString() }.returns("abcdefgh")
                    every { it.substring(3, 5) }.returns("abcdefgh".substring(3, 5))
                }
        every { editText.text }.returns(editable)

        val extracted = SelectedTextExtractor(editText).invoke()
        assertEquals("de", extracted)
        verify(atLeast = 0, atMost = 0) { editable.toString() }
        verify { editable.substring(3, 5) }
    }

    @Test
    fun testNotFoundCase() {
        val editText = mockk<EditText>()

        every { editText.selectionStart }.returns(-1)
        every { editText.selectionEnd }.returns(5)
        val editable = mockk<Editable>().also {
            every { it.toString() }.returns("abcdefgh")
        }
        every { editText.text }.returns(editable)

        val extracted = SelectedTextExtractor(editText).invoke()
        assertEquals("abcdefgh", extracted)
        verify { editable.toString() }
        verify(atLeast = 0, atMost = 0) { editable.substring(3, 5) }
    }
}