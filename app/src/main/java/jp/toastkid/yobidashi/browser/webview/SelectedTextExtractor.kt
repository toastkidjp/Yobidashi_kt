package jp.toastkid.yobidashi.browser.webview

import android.webkit.WebView

/**
 * @author toastkidjp
 */
class SelectedTextExtractor {

    fun withAction(webView: WebView, callback: (String) -> Unit) {
        webView.evaluateJavascript(SCRIPT) { callback(it) }
    }

    companion object {
        private const val SCRIPT = "document.getSelection().toString()"
    }
}