package jp.toastkid.yobidashi.browser

import android.support.annotation.IdRes
import jp.toastkid.yobidashi.R

/**
 * Browser screen mode.
 *
 * @author toastkidjp
 */
internal enum class ScreenMode(@IdRes private val id: Int) {

    FULL_SCREEN(R.id.full_screen),
    EXPANDABLE(R.id.expandable),
    FIXED(R.id.fixed);

    fun id(): Int = this.id
}