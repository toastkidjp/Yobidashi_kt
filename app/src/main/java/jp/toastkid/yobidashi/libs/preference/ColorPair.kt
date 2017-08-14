package jp.toastkid.yobidashi.libs.preference

import android.support.annotation.ColorInt

/**
 * @author toastkidjp
 */
class ColorPair(@param:ColorInt private val bgColor: Int, @param:ColorInt private val fontColor: Int) {

    fun bgColor(): Int {
        return bgColor
    }

    fun fontColor(): Int {
        return fontColor
    }
}
