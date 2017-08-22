package jp.toastkid.jitte.libs

import android.util.Log

import jp.toastkid.jitte.BuildConfig

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
