package jp.toastkid.yobidashi.settings.color

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

}
