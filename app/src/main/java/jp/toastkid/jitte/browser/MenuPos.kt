package jp.toastkid.jitte.browser

import android.support.annotation.IdRes
import android.support.design.widget.CoordinatorLayout
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import jp.toastkid.jitte.R

/**
 * @author toastkidjp
 */
enum class MenuPos private constructor(@param:IdRes internal val id: Int) {
    LEFT(R.id.menu_pos_left),
    RIGHT(R.id.menu_pos_right);

    @IdRes fun id(): Int {
        return id
    }

    companion object {

        fun place(
                fab: View,
                fabMarginBottom: Int,
                fabMarginHorizontal: Int,
                menuPos: MenuPos
        ) {
            val layoutParams = fab.layoutParams as ViewGroup.MarginLayoutParams
            val gravityParams = fab.layoutParams as CoordinatorLayout.LayoutParams
            when (menuPos) {
                LEFT -> {
                    gravityParams.gravity = Gravity.LEFT or Gravity.BOTTOM
                    layoutParams.setMargins(fabMarginHorizontal, 0, 0, fabMarginBottom)
                    fab.requestLayout()
                    return
                }
                RIGHT -> {
                    gravityParams.gravity = Gravity.RIGHT or Gravity.BOTTOM
                    layoutParams.setMargins(0, 0, fabMarginHorizontal, fabMarginBottom)
                    fab.requestLayout()
                    return
                }
            }
        }
    }

}
