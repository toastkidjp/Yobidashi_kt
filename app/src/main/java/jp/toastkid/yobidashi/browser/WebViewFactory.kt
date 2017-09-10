package jp.toastkid.yobidashi.browser

import android.content.Context
import android.view.MotionEvent
import android.webkit.WebView

/**
 * [WebView] factory.

 * @author toastkidjp
 */
internal object WebViewFactory {

    fun make(context: Context): CustomWebView {
        val webView = CustomWebView(context)
        webView.setOnTouchListener{ _, motionEvent ->
            when (motionEvent.getAction()) {
                MotionEvent.ACTION_UP -> webView.enablePullToRefresh = false
            }
            false
        }
        val settings = webView.settings
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.javaScriptCanOpenWindowsAutomatically = false
        return webView
    }
}
