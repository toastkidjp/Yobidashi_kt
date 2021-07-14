@file:Suppress("DEPRECATION")

package jp.toastkid.yobidashi.browser

import android.webkit.CookieManager

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
    operator fun invoke(callback: () -> Unit) {
        CookieManager.getInstance().removeAllCookies { callback() }
    }

}