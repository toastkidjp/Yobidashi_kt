package jp.toastkid.yobidashi.browser.webview

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.webkit.WebView

/**
 * Extend for disabling pull-to-refresh on Google map.
 *
 * @author toastkidjp
 */
internal class CustomWebView(context: Context) : WebView(context) {

    var enablePullToRefresh = false

    var scrollListener: (Int, Int, Int, Int) -> Unit = { _, _, _, _  -> }

    var scrolling: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            scrolling = 0
        }
        return super.onTouchEvent(event)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        enablePullToRefresh = scrolling <= 0
    }

    override fun onScrollChanged(horizontal: Int, vertical: Int, oldHorizontal: Int, oldVertical: Int) {
        super.onScrollChanged(horizontal, vertical, oldHorizontal, oldVertical)
        scrolling += vertical
        scrollListener(horizontal, vertical, oldHorizontal, oldVertical)
    }
}