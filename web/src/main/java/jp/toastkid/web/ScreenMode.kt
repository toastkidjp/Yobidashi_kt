package jp.toastkid.web

import androidx.annotation.IdRes

/**
 * WebTab UI screen mode.
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

            return entries.find { it.name == name } ?: EXPANDABLE
        }
    }
}