package jp.toastkid.jitte.settings.color

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Table

/**
 * @author toastkidjp
 */
@Table
class SavedColor {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var bgColor: Int = 0

    @Column
    var fontColor: Int = 0

}
