package jp.toastkid.yobidashi.browser

import android.content.Context
import android.webkit.WebView

/**
 * Extend for disabling pull-to-refresh on Google map.
 *
 * @author toastkidjp
 */
class CustomWebView(context: Context) : WebView(context) {

    var enablePullToRefresh = false

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        enablePullToRefresh = true
    }
}