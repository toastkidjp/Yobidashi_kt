@file:Suppress("DEPRECATION")

package jp.toastkid.yobidashi.browser

import android.content.Context
import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.lib.preference.PreferenceApplier

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies { callback() }
            return
        }
        invokeUnderLollipop(context)
        callback()
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