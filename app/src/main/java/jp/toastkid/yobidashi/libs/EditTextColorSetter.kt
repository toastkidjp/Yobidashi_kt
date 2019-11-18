package jp.toastkid.yobidashi.libs

import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

/**
 * Color utilities.
 *
 * @author toastkidjp
 */
class EditTextColorSetter {

    /**
     * Set specified color to passed EditText.
     *
     * @param editText
     * @param fontColor
     */
    fun invoke(editText: EditText, @ColorInt fontColor: Int) {
        editText.apply {
            setTextColor(fontColor)
            setHintTextColor(fontColor)
            highlightColor = ColorUtils.setAlphaComponent(fontColor, 128)
        }
    }

}
