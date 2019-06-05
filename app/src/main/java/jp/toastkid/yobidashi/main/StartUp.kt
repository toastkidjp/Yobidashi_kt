package jp.toastkid.yobidashi.main

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import jp.toastkid.yobidashi.R

/**
 * Start-up view definition.
 *
 * @param titleId
 * @param radioButtonId
 *
 * @author toastkidjp
 */
enum class StartUp(@StringRes val titleId: Int, @IdRes val radioButtonId: Int) {
    START(R.string.title_startup_start, R.id.start_up_start),
    SEARCH(R.string.title_search, R.id.start_up_search),
    BROWSER(R.string.title_browser, R.id.start_up_browser),
    APPS_LAUNCHER(R.string.title_apps_launcher, R.id.start_up_launcher);

    companion object {

        /**
         * Find value by name.
         *
         * @param name [String]
         */
        fun findByName(name: String): StartUp = if (name.isEmpty()) getDefault() else valueOf(name)

        /**
         * Find value by ID.
         *
         * @param checkedRadioButtonId [Int]
         */
        fun findById(@IdRes checkedRadioButtonId: Int): StartUp =
                values().find { it.radioButtonId == checkedRadioButtonId } ?: getDefault()

        /**
         * Return default value.
         */
        private fun getDefault(): StartUp = BROWSER
    }
}