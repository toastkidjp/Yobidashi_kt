package jp.toastkid.yobidashi.analytics

import android.content.Context
import android.os.Bundle

import com.google.firebase.analytics.FirebaseAnalytics

import jp.toastkid.yobidashi.BuildConfig

/**
 * Analytics logger wrapper.
 *
 * @param context Initialize with [Context]
 * @author toastkidjp
 */
class LogSender(context: Context) {

    /**
     * Firebase analytics log sender.
     */
    private val sender: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    /**
     * Send log only release build.
     *
     * @param key log's key
     * @param bundle default: EMPTY
     */
    @JvmOverloads
    fun send(key: String, bundle: Bundle = Bundle.EMPTY) {
        if (BuildConfig.DEBUG) {
            return
        }
        sender.logEvent(key, bundle)
    }

}