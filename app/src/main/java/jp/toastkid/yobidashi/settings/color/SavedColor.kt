package jp.toastkid.yobidashi.settings.color

import android.widget.TextView
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author toastkidjp
 */
@Entity
class SavedColor {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var bgColor: Int = 0

    var fontColor: Int = 0

    /**
     * Set saved colors.
     *
     * @param tv
     */
    fun setTo(tv: TextView) {
        tv.setBackgroundColor(bgColor)
        tv.setTextColor(fontColor)
    }
}
