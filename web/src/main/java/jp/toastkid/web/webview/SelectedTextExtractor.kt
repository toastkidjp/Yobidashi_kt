package jp.toastkid.web.webview

import android.webkit.WebView

/**
 * @author toastkidjp
 */
class SelectedTextExtractor {

    fun withAction(webView: WebView, callback: (String) -> Unit) {
        webView.evaluateJavascript(SCRIPT) {
            callback(it)
        }
    }

    companion object {
        private const val SCRIPT = "window.getSelection().toString()"
    }
}