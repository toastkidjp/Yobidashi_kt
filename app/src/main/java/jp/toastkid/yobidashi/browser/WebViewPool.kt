package jp.toastkid.yobidashi.browser

import android.content.Context
import android.util.LongSparseArray
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * @author toastkidjp
 */
internal class WebViewPool(private val context: Context, private val webViewClient: WebViewClient, private val webChromeClient: WebChromeClient) {

    private val pool: LongSparseArray<WebView>

    init {
        pool = LongSparseArray<WebView>(MAXIMUM_POOL_SIZE)
    }

    fun get(): WebView {
        if (pool.size() < MAXIMUM_POOL_SIZE) {
            val webView = WebViewFactory.make(context)
            webView.setWebViewClient(webViewClient)
            webView.setWebChromeClient(webChromeClient)
            pool.append(System.nanoTime(), webView)
            return webView
        }

        val webView = WebViewFactory.make(context)
        webView.setWebViewClient(webViewClient)
        webView.setWebChromeClient(webChromeClient)
        pool.append(System.nanoTime(), webView)
        return webView
    }

    companion object {

        private val MAXIMUM_POOL_SIZE = 10
    }

}
