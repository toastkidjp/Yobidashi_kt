package jp.toastkid.web.webview

import android.webkit.WebView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.web.R

/**
 * @author toastkidjp
 */
class SelectedTextExtractor {

    fun withAction(webView: WebView, callback: (String) -> Unit) {
        webView.evaluateJavascript(SCRIPT) {
            if (it.length < 2) {
                (webView.context as? ViewModelStoreOwner)?.let { owner ->
                    ViewModelProvider(owner).get(ContentViewModel::class)
                        .snackShort(R.string.message_failed_query_extraction_from_web_view)
                }
                return@evaluateJavascript
            }
            callback(it)
        }
    }

    companion object {
        private const val SCRIPT = "(function() { return window.getSelection().toString(); })();"
    }
}