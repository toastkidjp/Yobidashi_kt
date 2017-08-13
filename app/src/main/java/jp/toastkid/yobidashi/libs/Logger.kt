package jp.toastkid.yobidashi.libs

import android.util.Log

import jp.toastkid.yobidashi.BuildConfig

/**
 * @author toastkidjp
 */
object Logger {

    fun i(s: String) {
        if (!BuildConfig.DEBUG) {
            return
        }
        Log.i("Logger", s)
    }
}
