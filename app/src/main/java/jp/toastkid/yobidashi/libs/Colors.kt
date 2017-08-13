package jp.toastkid.yobidashi.libs

import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import android.widget.EditText
import android.widget.TextView

import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * Color utilities.

 * @author toastkidjp
 */
object Colors {

    /**
     * Set specified color to passed EditText.
     * @param editText
     * *
     * @param fontColor
     */
    fun setEditTextColor(editText: EditText, @ColorInt fontColor: Int) {
        editText.setTextColor(fontColor)
        editText.setHintTextColor(fontColor)
        editText.highlightColor = ColorUtils.setAlphaComponent(fontColor, 128)
    }

    fun setBgAndText(
            tv: TextView,
            pair: ColorPair
    ) {
        tv.setBackgroundColor(pair.bgColor())
        tv.setTextColor(pair.fontColor())
    }
}
