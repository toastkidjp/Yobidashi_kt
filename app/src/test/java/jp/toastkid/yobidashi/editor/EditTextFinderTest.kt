package jp.toastkid.yobidashi.editor

import android.widget.EditText
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * [EditTextFinder]'s test cases.
 *
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class EditTextFinderTest {

    /**
     * [EditText]
     */
    private lateinit var editText: EditText

    /**
     * [EditTextFinder]
     */
    private lateinit var finder: EditTextFinder

    /**
     * Initialize test target instances.
     */
    @Before
    fun setUp() {
        editText = EditText(RuntimeEnvironment.application)
        finder = EditTextFinder(editText)

        editText.setText("abc is abc, you don't find abcd.")
    }

    /**
     * Test [EditTextFinder.findUp] behavior.
     */
    @Test
    fun findUp() {
        finder.findUp("abc")
        assertEquals(27, editText.selectionStart)
        assertEquals(30, editText.selectionEnd)

        finder.findUp("abc")
        assertEquals(7, editText.selectionStart)
        assertEquals(10, editText.selectionEnd)
    }

    /**
     * Test [EditTextFinder.findDown] behavior.
     */
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