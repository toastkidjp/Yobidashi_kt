package jp.toastkid.yobidashi.media.music.popup.playback.speed

import android.widget.TextView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jp.toastkid.yobidashi.libs.preference.ColorPair
import org.junit.Test

/**
 * @author toastkidjp
 */
class ViewHolderTest {

    @Test
    fun testBind() {
        val textView = mockk<TextView>()
        every { textView.setText(any<Int>()) }.answers { Unit }
        val colorPair = mockk<ColorPair>()
        every { colorPair.setTo(any()) }.answers { Unit }
        val viewHolder = ViewHolder(textView)

        viewHolder.bind(PlayingSpeed.NORMAL, colorPair)

        verify(atLeast = 1) { textView.setText(any<Int>()) }
        verify(atLeast = 1) { colorPair.setTo(textView) }
    }
}