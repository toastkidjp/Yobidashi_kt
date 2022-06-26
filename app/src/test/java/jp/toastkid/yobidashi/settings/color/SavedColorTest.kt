package jp.toastkid.yobidashi.settings.color

import android.graphics.Color
import android.widget.TextView
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
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
        val savedColor = SavedColor.make(Color.BLACK, Color.WHITE)

        assertEquals(Color.BLACK, savedColor.bgColor)
        assertEquals(Color.WHITE, savedColor.fontColor)
    }
}