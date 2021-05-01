package jp.toastkid.yobidashi.libs.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Network checker.
 *
 * @author toastkidjp
 */
@Deprecated("ActiveNetworkInfo is deprecated.")
object NetworkChecker {

    enum class NetworkType {
        WIFI, OTHER, NONE
    }

    /**
     * Return true if we can't use network.
     *
     * @param context
     * @return
     */
    fun isNotAvailable(context: Context): Boolean {
        return isAvailable(context) == NetworkType.NONE
    }

    /**
     * Check usable network.
     *
     * @param context
     * @return
     */
    private fun isAvailable(context: Context): NetworkType {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                        ?: return NetworkType.NONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return NetworkType.NONE
            val networkCapabilities =
                    connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkType.NONE
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.OTHER
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.OTHER
                else -> NetworkType.NONE
            }
        }

        return connectivityManager.activeNetworkInfo?.let {
            when (it.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
                ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_ETHERNET-> NetworkType.OTHER
                else -> NetworkType.NONE
            }
        } ?: NetworkType.NONE
    }

}
