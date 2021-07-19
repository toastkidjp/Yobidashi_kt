package jp.toastkid.yobidashi.libs.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Network checker.
 *
 * @author toastkidjp
 */
object NetworkChecker {

    private enum class NetworkType {
        WIFI, OTHER, NONE
    }

    /**
     * If it can't use network, return true.
     *
     * @param context Use for obtaining ConnectivityManager
     * @return If it can't use network, return true
     */
    fun isNotAvailable(context: Context): Boolean {
        return isAvailable(context) == NetworkType.NONE
    }

    /**
     * If current network is not Wi-Fi or it is unavailable, return true.
     *
     * @param context Use for obtaining ConnectivityManager
     * @return If current network is not Wi-Fi or it is unavailable, return true.
     */
    fun isUnavailableWiFi(context: Context): Boolean {
        return isAvailable(context) != NetworkType.WIFI
    }

    /**
     * Check network is available.
     *
     * @param context Use for obtaining [ConnectivityManager]
     * @return If network is available, return true
     */
    private fun isAvailable(context: Context): NetworkType {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                        ?: return NetworkType.NONE
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

}
