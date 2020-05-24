@file:Suppress("DEPRECATION")

package jp.toastkid.yobidashi.browser

import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Cookie cleaner for backward compatible.
 *
 * @author toastkidjp
 */
class CookieCleanerCompat {

    /**
     * Invoke action.
     *
     * @param snackbarParent
     */
    operator fun invoke(snackbarParent: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies { snackMessage(snackbarParent) }
            return
        }
        invokeUnderLollipop(snackbarParent)
    }

    /**
     * Invoke under lollipop environment.
     *
     * @param snackbarParent
     */
    private fun invokeUnderLollipop(snackbarParent: View) {
        val cookieSyncManager = CookieSyncManager.createInstance(snackbarParent.context)
        cookieSyncManager.startSync()
        CookieManager.getInstance().run {
            removeAllCookie()
            removeSessionCookie()
        }
        cookieSyncManager.stopSync()
        cookieSyncManager.sync()
        snackMessage(snackbarParent)
    }

    /**
     * Show message with snackbar.
     *
     * @param snackbarParent
     */
    private fun snackMessage(snackbarParent: View) {
        Toaster.snackShort(
                snackbarParent,
                R.string.done_clear,
                PreferenceApplier(snackbarParent.context).colorPair()
        )
    }
}