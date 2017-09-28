package jp.toastkid.yobidashi.libs.preference

import android.support.annotation.ColorInt

/**
 * Color pair of toolbar and so on...
 *
 * @author toastkidjp
 */
class ColorPair(
        @param:ColorInt private val bgColor:   Int,
        @param:ColorInt private val fontColor: Int
) {

    @ColorInt fun bgColor():   Int = bgColor

    @ColorInt fun fontColor(): Int = fontColor
}
