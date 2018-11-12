package jp.toastkid.yobidashi.browser.webview

import android.os.Build
import android.webkit.WebView

/**
 * @author toastkidjp
 */
object SelectedTextExtractor {

    private const val SCRIPT = "(function(){ return document.getSelection().toString(); })()"

    fun withAction(webView: WebView, callback: (String) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(SCRIPT) { callback(it) }
        }
    }
}