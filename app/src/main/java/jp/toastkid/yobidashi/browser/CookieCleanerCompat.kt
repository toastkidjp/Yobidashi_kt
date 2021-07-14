@file:Suppress("DEPRECATION")

package jp.toastkid.yobidashi.browser

import android.content.Context
import android.webkit.CookieManager
import android.webkit.CookieSyncManager

/**
 * Cookie cleaner for backward compatible.
 *
 * @author toastkidjp
 */
class CookieCleanerCompat {

    /**
     * Invoke action.
     *
     * @param context Use for under lollipop devices
     * @param callback Pass action on complete work.
     */
    operator fun invoke(context: Context, callback: () -> Unit) {
        CookieManager.getInstance().removeAllCookies { callback() }
    }

    /**
     * Invoke under lollipop environment.
     *
     * @param context Use for under lollipop devices
     */
    private fun invokeUnderLollipop(context: Context) {
        val cookieSyncManager = CookieSyncManager.createInstance(context)
        cookieSyncManager.startSync()
        CookieManager.getInstance().run {
            removeAllCookie()
            removeSessionCookie()
        }
        cookieSyncManager.stopSync()
        cookieSyncManager.sync()
    }

}