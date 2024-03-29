package jp.toastkid.editor

import android.text.Editable
import android.widget.EditText
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [SelectedTextExtractor]'s test cases.
 *
 * @author toastkidjp
 */
class SelectedTextExtractorTest {

    /**
     * Check behavior of found target text case.
     */
    @Test
    fun testFoundCase() {
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

    /**
     * Check behavior of not found target text case.
     */
    @Test
    fun testNotFoundCase() {
        val editText = mockk<EditText>()

        every { editText.selectionStart }.returns(0)
        every { editText.selectionEnd }.returns(0)
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