package jp.toastkid.jitte.main

import android.support.annotation.StringRes
import jp.toastkid.jitte.R

/**
 * @author toastkidjp
 */
enum class StartUp(@StringRes val titleId: Int) {
    START(R.string.title_startup_start),
    SEARCH(R.string.title_search),
    BROWSER(R.string.title_browser),
    APPS_LAUNCHER(R.string.title_apps_launcher);

    companion object {
        fun find(name: String): StartUp {
            if (name.isEmpty()) {
                return getDefault()
            }
            return valueOf(name)
        }

        fun getDefault(): StartUp {
            return START
        }

        fun findIndex(startUp: StartUp): Int? {
            return (0..values().size - 1).find { values()[it] == startUp }
        }
    }
}