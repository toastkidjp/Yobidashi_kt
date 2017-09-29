package jp.toastkid.yobidashi.main

import android.support.annotation.IdRes
import android.support.annotation.StringRes
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
        fun find(name: String): StartUp {
            if (name.isEmpty()) {
                return getDefault()
            }
            return valueOf(name)
        }

        private fun getDefault(): StartUp = START

        fun findById(@IdRes checkedRadioButtonId: Int): StartUp =
                values().find { it.radioButtonId == checkedRadioButtonId } ?: START
    }
}