package jp.toastkid.yobidashi.libs

import jp.toastkid.yobidashi.TestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class TextInputsTest {

    @Test
    fun testMake() {
        val textInputLayout = TextInputs.make(RuntimeEnvironment.application)
        assertNotNull(textInputLayout.editText)
    }

    @Test
    fun testWithDefaultInput() {
        val defaultInput = "default"
        val textInputLayout = TextInputs.withDefaultInput(RuntimeEnvironment.application, defaultInput)
        val editText = textInputLayout.editText

        assertNotNull(editText)
        assertEquals(defaultInput, editText?.text.toString())
        assertEquals(defaultInput.length, editText?.selectionStart)
        assertEquals(defaultInput.length, editText?.selectionEnd)
    }
}