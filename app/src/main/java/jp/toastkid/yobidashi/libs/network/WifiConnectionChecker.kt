package jp.toastkid.yobidashi.libs.network

import android.content.Context
import android.net.ConnectivityManager

/**
 * Wi-Fi connection checker.
 * TODO Rewrite new API
 * @author toastkidjp
 */
object WifiConnectionChecker {

    /**
     * Check is not connecting Wi-Fi.
     *
     * @param context
     * @return is <b>not</b> connecting Wi-Fi
     */
    fun isNotConnecting(context: Context): Boolean {
        val cm = context.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager?

        return when (cm?.activeNetworkInfo?.type) {
            ConnectivityManager.TYPE_WIFI -> false
            ConnectivityManager.TYPE_WIMAX -> false
            else -> true
        }
    }

}