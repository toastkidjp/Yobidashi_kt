package jp.toastkid.yobidashi.browser

import android.os.Build
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread

class HtmlSourceExtractionUseCase {

    private val script = "document.documentElement.outerHTML;"

    @UiThread
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    operator fun invoke(webView: WebView?, callback: ValueCallback<String>) {
        if (webView?.url?.startsWith("https://accounts.google.com/signin") == true) {
            return
        }

        webView?.evaluateJavascript(script, callback)
    }
}
