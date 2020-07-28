package jp.toastkid.yobidashi.settings.color

import android.widget.TextView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class SavedColorTest {

    @MockK
    private lateinit var textView: TextView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        every { textView.setTextColor(any<Int>()) }.answers { Unit }
        every { textView.setBackgroundColor(any()) }.answers { Unit }

        SavedColor().setTo(textView)

        verify(exactly = 1) { textView.setTextColor(any<Int>()) }
        verify(exactly = 1) { textView.setBackgroundColor(any()) }
    }
}