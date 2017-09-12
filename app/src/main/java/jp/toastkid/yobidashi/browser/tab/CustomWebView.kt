package jp.toastkid.yobidashi.browser.tab

import android.content.Context
import android.webkit.WebView

/**
 * Extend for disabling pull-to-refresh on Google map.
 *
 * @author toastkidjp
 */
internal class CustomWebView(context: Context) : WebView(context) {

    var enablePullToRefresh = false

    var scrollListener: (Int, Int, Int, Int) -> Unit = { _, _, _, _  -> }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        enablePullToRefresh = true
    }

    override fun onScrollChanged(horizontal: Int, vertical: Int, oldHorizontal: Int, oldVertical: Int) {
        super.onScrollChanged(horizontal, vertical, oldHorizontal, oldVertical)
        scrollListener(horizontal, vertical, oldHorizontal, oldVertical)
    }
}