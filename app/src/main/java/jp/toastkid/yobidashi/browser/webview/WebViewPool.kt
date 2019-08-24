package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.os.Build
import android.util.LruCache
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * [WebView] pool.
 *
 * @param context Use for make [WebViewFactory] instance.
 * @param webViewClientSupplier
 * @param webChromeClientSupplier
 * @param scrollCallback Use for implementing action on scroll
 * @param poolSize (Optional) Count of containing [WebView] instance. If you don't passed it,
 * it use default size.
 *
 * @author toastkidjp
 */
internal class WebViewPool(
        private val context: Context,
        private val webViewClientSupplier: () -> WebViewClient,
        private val webChromeClientSupplier: () -> WebChromeClient,
        poolSize: Int = DEFAULT_MAXIMUM_POOL_SIZE
) {

    /**
     * Containing [WebView] instance.
     */
    private val pool: LruCache<String, WebView>

    private val alphaConverter = AlphaConverter()

    /**
     * Latest tab's ID.
     */
    private var latestTabId: String? = null

    init {
        pool = LruCache(if (0 < poolSize) poolSize else DEFAULT_MAXIMUM_POOL_SIZE)
    }

    /**
     * Get specified [WebView] by tab ID.
     *
     * @param tabId tab ID
     * @return [WebView] (Nullable)
     */
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

    /**
     * Get latest [WebView].
     *
     * @return [WebView] (Nullable)
     */
    fun getLatest(): WebView? = latestTabId?.let { pool.get(it) }

    /**
     * Remove [WebView] by tab ID.
     *
     * @param tabId tab ID
     */
    fun remove(tabId: String?) {
        if (tabId == null) {
            return
        }
        pool.remove(tabId)
    }

    /**
     * Resize poll size.
     *
     * @param newSize new pool size
     */
    fun resize(newSize: Int) {
        if (newSize == pool.maxSize()) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            && 0 < newSize) {
            pool.resize(newSize)
        }
    }


    fun applyNewAlpha() {
        val newAlphaBackground = alphaConverter.readBackground(context)
        pool.snapshot().values.forEach { it.setBackgroundColor(newAlphaBackground) }
    }

    /**
     * Destroy all [WebView].
     */
    fun dispose() {
        pool.snapshot().values.forEach { it.destroy() }
    }

    companion object {

        /**
         * Default pool size.
         */
        private const val DEFAULT_MAXIMUM_POOL_SIZE = 6
    }

}
