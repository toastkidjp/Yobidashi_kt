package jp.toastkid.jitte.browser

import android.content.Context
import android.webkit.WebView

/**
 * [WebView] factory.

 * @author toastkidjp
 */
internal object WebViewFactory {

    fun make(context: Context): WebView {
        val webView = WebView(context)
        val settings = webView.settings
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.javaScriptCanOpenWindowsAutomatically = false
        return webView
    }
}
