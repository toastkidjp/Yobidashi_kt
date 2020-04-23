package jp.toastkid.yobidashi.libs.preference

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

    /**
     * Set background and text color.
     *
     * @param tv
     */
    fun setTo(tv: TextView) {
        tv.setBackgroundColor(bgColor)
        tv.setTextColor(fontColor)
        tv.compoundDrawables.forEach {
            it?.colorFilter = PorterDuffColorFilter(fontColor, PorterDuff.Mode.SRC_IN)
        }
    }

    /**
     * Apply colors to passed [FloatingActionButton].
     *
     * @param floatingActionButton [FloatingActionButton]
     */
    fun applyTo(floatingActionButton: FloatingActionButton?) {
        floatingActionButton?.backgroundTintList = ColorStateList.valueOf(bgColor)
        floatingActionButton?.drawable?.also {
            DrawableCompat.setTint(it, fontColor)
            floatingActionButton.setImageDrawable(it)
        }
    }
}
