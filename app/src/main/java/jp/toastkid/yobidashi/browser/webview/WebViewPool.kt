package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.os.Build
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
        private val webChromeClientSupplier: () -> WebChromeClient,
        private val loader: (String, Boolean) -> Unit,
        poolSize: Int = DEFAULT_MAXIMUM_POOL_SIZE
) {

    private val pool: LruCache<String, WebView>

    private var latestTabId: String? = null

    init {
        pool = LruCache(if (0 < poolSize) poolSize else DEFAULT_MAXIMUM_POOL_SIZE)
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

        val webView = WebViewFactory.make(context, loader)
        webView.webViewClient = webViewClientSupplier()
        webView.webChromeClient = webChromeClientSupplier()
        pool.put(tabId, webView)
        return webView
    }

    fun getLatest(): WebView? = latestTabId?.let { pool.get(it) }

    fun remove(tabId: String?) {
        if (tabId == null) {
            return
        }
        pool.remove(tabId)
    }

    fun resize(newSize: Int) {
        if (newSize == pool.maxSize()) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            && 0 < newSize) {
            pool.resize(newSize)
        }
    }

    fun dispose() {
        pool.snapshot().values.forEach { it.destroy() }
    }

    companion object {

        private const val DEFAULT_MAXIMUM_POOL_SIZE = 6
    }

}
