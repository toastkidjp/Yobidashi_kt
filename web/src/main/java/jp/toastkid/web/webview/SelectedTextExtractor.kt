package jp.toastkid.web.webview

import android.webkit.WebView

/**
 * @author toastkidjp
 */
class SelectedTextExtractor {

    fun withAction(webView: WebView, callback: (String) -> Unit) {
        webView.evaluateJavascript(SCRIPT) {
            println(it)
            callback(it)
        }
    }

    companion object {
        private const val SCRIPT = "window.getSelection().toString()"
    }
}