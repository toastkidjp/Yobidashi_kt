package jp.toastkid.yobidashi.editor

import android.text.Editable
import android.widget.EditText
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class StringSurroundingUseCaseTest {

    @InjectMockKs
    private lateinit var useCase: StringSurroundingUseCase

    @MockK
    private lateinit var text: Editable

    @MockK
    private lateinit var editText: EditText

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { text.subSequence(any(), any()) }.returns("tomato")
        every { text.replace(any(), any(), any<String>()) }.returns(mockk())

        every { editText.text }.returns(text)
        every { editText.selectionStart }.returns(2)
        every { editText.selectionEnd }.returns(3)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        useCase.invoke(editText, '"')

        verify(atLeast = 1) { text.replace(2, 3, any<String>()) }
    }

    @Test
    fun test() {
        useCase.invoke(editText, "~~")

        verify(atLeast = 1) { text.replace(2, 3, any<String>()) }
    }

}