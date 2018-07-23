package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.util.LruCache
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * @author toastkidjp
 */
internal class WebViewPool(
        private val context: Context,
        private val webViewClientSupplier: () -> WebViewClient,
        private val webChromeClientSupplier: () -> WebChromeClient
) {

    private val pool: LruCache<String, WebView>

    private var latestTabId: String? = null

    init {
        pool = LruCache(MAXIMUM_POOL_SIZE)
    }

    fun get(tabId: String?): WebView? {
        if (tabId == null) {
            return null
        }

        latestTabId = tabId

        val extract = pool[tabId]
        if (extract != null) {
            return extract
        }

        val webView = WebViewFactory.make(context)
        webView.webViewClient = webViewClientSupplier()
        webView.webChromeClient = webChromeClientSupplier()
        pool.put(tabId, webView)
        return webView
    }

    fun getLatest(): WebView? = latestTabId?.let { pool.get(it) }

    companion object {

        private const val MAXIMUM_POOL_SIZE = 3
    }

}
