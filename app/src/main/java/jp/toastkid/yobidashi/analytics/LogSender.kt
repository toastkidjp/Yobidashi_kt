package jp.toastkid.yobidashi.analytics

import android.content.Context
import android.os.Bundle

import com.google.firebase.analytics.FirebaseAnalytics

import jp.toastkid.yobidashi.BuildConfig

/**
 * Analytics logger wrapper.

 * @author toastkidjp
 */
class LogSender
/**
 * Initialize with [Context].
 * @param context
 */
(context: Context) {

    /** Firebase analytics log sender.  */
    private val sender: FirebaseAnalytics

    init {
        sender = FirebaseAnalytics.getInstance(context)
    }

    /**
     * Send log.
     * @param key
     * *
     * @param bundle
     */
    @JvmOverloads fun send(key: String, bundle: Bundle = Bundle.EMPTY) {
        if (BuildConfig.DEBUG) {
            return
        }
        sender.logEvent(key, bundle)
    }

}
/**
 * Send empty parameter log.
 * @param key
 */
