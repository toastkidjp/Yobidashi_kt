package jp.toastkid.yobidashi.libs.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * Network checker.

 * @author toastkidjp
 */
object NetworkChecker {

    /**
     * Return true if we can't use network.
     * @param context
     * *
     * @return
     */
    fun isNotAvailable(context: Context): Boolean {
        return !isAvailable(context)
    }

    /**
     * Check usable network.
     * @param context
     * *
     * @return
     */
    fun isAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo ?: return false
        return info.isConnected
    }
}
