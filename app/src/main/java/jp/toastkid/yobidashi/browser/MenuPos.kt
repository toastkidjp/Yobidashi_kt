package jp.toastkid.yobidashi.browser

import android.support.annotation.IdRes
import com.cleveroad.cyclemenuwidget.CycleMenuWidget
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
enum class MenuPos(
        @param:IdRes internal val id: Int,
        val corner: CycleMenuWidget.CORNER
        ) {
    LEFT(R.id.menu_pos_left, CycleMenuWidget.CORNER.LEFT_BOTTOM),
    RIGHT(R.id.menu_pos_right, CycleMenuWidget.CORNER.RIGHT_BOTTOM);

    @IdRes fun id(): Int = id

}
