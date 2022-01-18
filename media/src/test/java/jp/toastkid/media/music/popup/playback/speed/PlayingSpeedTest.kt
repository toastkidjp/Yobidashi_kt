package jp.toastkid.media.music.popup.playback.speed

import jp.toastkid.media.R
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author toastkidjp
 */
@RunWith(RobolectricTestRunner::class)
class PlayingSpeedTest {

    @Test
    fun test() {
        assertSame(
                PlayingSpeed.getDefault(),
                PlayingSpeed.findById(-1L)
        )
        assertSame(
                PlayingSpeed.S_0_7,
                PlayingSpeed.findById(R.string.title_playing_speed_0_7.toLong())
        )
    }
}