package jp.toastkid.yobidashi.browser.webview

import android.os.Build
import android.util.LruCache
import android.webkit.WebView
import androidx.annotation.ColorInt

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
internal class WebViewPool(poolSize: Int = DEFAULT_MAXIMUM_POOL_SIZE) {

    /**
     * Containing [WebView] instance.
     */
    private val pool: LruCache<String, WebView>

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

        return pool[tabId]
    }

    /**
     * Get latest [WebView].
     *
     * @return [WebView] (Nullable)
     */
    fun getLatest(): WebView? = latestTabId?.let { return@let pool.get(it) }

    fun put(tabId: String, webView: WebView) {
        pool.put(tabId, webView)
    }

    fun containsKey(tabId: String?) = tabId != null && pool.snapshot().containsKey(tabId)

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

    fun applyNewAlpha(@ColorInt newAlphaBackground: Int) {
        pool.snapshot().values.forEach { it.setBackgroundColor(newAlphaBackground) }
    }

    fun onResume() {
        getLatest()?.resumeTimers()
        pool.snapshot().values.forEach { it.onResume() }
    }

    fun onPause() {
        getLatest()?.pauseTimers()
        pool.snapshot().values.forEach { it.onPause() }
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
