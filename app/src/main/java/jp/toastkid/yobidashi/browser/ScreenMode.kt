package jp.toastkid.yobidashi.browser

import androidx.annotation.IdRes

/**
 * Browser screen mode.
 *
 * @author toastkidjp
 */
internal enum class ScreenMode(@IdRes private val id: Int) {

    FULL_SCREEN(1),
    EXPANDABLE(2),
    FIXED(3);

    fun id(): Int = this.id

    companion object {
        fun find(name: String?): ScreenMode {
            if (name.isNullOrBlank()) {
                return EXPANDABLE
            }

            return values().find { it.name == name } ?: EXPANDABLE
        }
    }
}