package jp.toastkid.yobidashi.editor

import android.widget.EditText
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class EditTextFinderTest {

    private lateinit var editText: EditText

    private lateinit var finder: EditTextFinder

    @Before
    fun setUp() {
        editText = EditText(RuntimeEnvironment.application)
        finder = EditTextFinder(editText)

        editText.setText("abc is abc, you don't find abcd.")
    }

    @Test
    fun findUp() {
        finder.findUp("abc")
        assertEquals(27, editText.selectionStart)
        assertEquals(30, editText.selectionEnd)

        finder.findUp("abc")
        assertEquals(7, editText.selectionStart)
        assertEquals(10, editText.selectionEnd)
    }

    @Test
    fun findDown() {
        finder.findDown("abc")
        assertEquals(0, editText.selectionStart)
        assertEquals(3, editText.selectionEnd)

        finder.findDown("abc")
        assertEquals(7, editText.selectionStart)
        assertEquals(10, editText.selectionEnd)
    }
}