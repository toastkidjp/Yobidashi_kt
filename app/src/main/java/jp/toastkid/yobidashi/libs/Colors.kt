package jp.toastkid.yobidashi.libs

import android.graphics.drawable.Drawable
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Color utilities.
 *
 * @author toastkidjp
 */
object Colors {

    /**
     * Set specified color to passed EditText.
     *
     * @param editText
     * @param fontColor
     */
    fun setEditTextColor(editText: EditText, @ColorInt fontColor: Int) {
        editText.apply {
            setTextColor(fontColor)
            setHintTextColor(fontColor)
            highlightColor = ColorUtils.setAlphaComponent(fontColor, 128)
        }
    }

    fun applyTint(icon: Drawable?, @ColorInt fontColor: Int) =
            icon?.let { DrawableCompat.setTint(it, fontColor) }
}
