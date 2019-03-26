package jp.toastkid.yobidashi.browser

import androidx.annotation.IdRes
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
enum class MenuPos(
        @param:IdRes internal val id: Int
        ) {
    LEFT(R.id.menu_pos_left),
    RIGHT(R.id.menu_pos_right);

    @IdRes fun id(): Int = id

}
