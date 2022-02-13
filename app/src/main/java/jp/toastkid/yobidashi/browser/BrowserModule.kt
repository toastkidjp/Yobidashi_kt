package jp.toastkid.yobidashi.browser

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.rss.suggestion.RssAddingSuggestion
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.Archive
import jp.toastkid.yobidashi.browser.archive.IdGenerator
import jp.toastkid.yobidashi.browser.archive.auto.AutoArchive
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.yobidashi.browser.download.image.AllImageDownloaderUseCase
import jp.toastkid.yobidashi.browser.page_information.PageInformationExtractor
import jp.toastkid.yobidashi.browser.reader.ReaderModeUseCase
import jp.toastkid.yobidashi.browser.usecase.HtmlSourceExtractionUseCase
import jp.toastkid.yobidashi.browser.usecase.WebViewReplacementUseCase
import jp.toastkid.yobidashi.browser.webview.AlphaConverter
import jp.toastkid.yobidashi.browser.webview.CustomViewSwitcher
import jp.toastkid.yobidashi.browser.webview.CustomWebView
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.browser.webview.WebViewFactoryUseCase
import jp.toastkid.yobidashi.browser.webview.WebViewStateUseCase
import jp.toastkid.yobidashi.browser.webview.factory.WebChromeClientFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewClientFactory
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.main.MainActivity
import timber.log.Timber

/**
 * @author toastkidjp
 */
class BrowserModule(
    private val context: Context,
    private val webViewContainer: FrameLayout?
) {

    private val preferenceApplier = PreferenceApplier(context)

    private val rssAddingSuggestion = RssAddingSuggestion(preferenceApplier)

    private val faviconApplier: FaviconApplier = FaviconApplier(context)

    private val readerModeUseCase by lazy { ReaderModeUseCase() }

    private var customViewSwitcher: CustomViewSwitcher? = null

    private val adRemover: AdRemover = AdRemover.make(context.assets)

    private val autoArchive = AutoArchive.make(context)

    private var browserHeaderViewModel: BrowserHeaderViewModel? = null

    private var loadingViewModel: LoadingViewModel? = null

    /**
     * Animation of slide up bottom.
     */
    private val slideUpFromBottom
            = AnimationUtils.loadAnimation(context, R.anim.slide_up)

    private val webViewReplacementUseCase: WebViewReplacementUseCase

    private val idGenerator = IdGenerator()

    private var contentViewModel: ContentViewModel? = null

    private val webViewFactory: WebViewFactoryUseCase

    private val alphaConverter = AlphaConverter()

    init {
        GlobalWebViewPool.resize(preferenceApplier.poolSize)

        customViewSwitcher = CustomViewSwitcher({ context }, { currentView() })

        if (context is MainActivity) {
            val viewModelProvider = ViewModelProvider(context)
            browserHeaderViewModel = viewModelProvider.get(BrowserHeaderViewModel::class.java)
            loadingViewModel = viewModelProvider.get(LoadingViewModel::class.java)
            contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
        }

        webViewFactory = WebViewFactoryUseCase(
                webViewClientFactory = WebViewClientFactory(
                    contentViewModel,
                    adRemover,
                    faviconApplier,
                    preferenceApplier,
                    browserHeaderViewModel,
                    rssAddingSuggestion,
                    loadingViewModel
                ) { currentView() },
                webChromeClientFactory = WebChromeClientFactory(
                        browserHeaderViewModel,
                        faviconApplier,
                        customViewSwitcher
                )
        )

        webViewReplacementUseCase = WebViewReplacementUseCase(
                webViewContainer,
                WebViewStateUseCase.make(context),
                { webViewFactory(context) },
                browserHeaderViewModel,
                preferenceApplier,
                slideUpFromBottom
        )
    }

    fun loadWithNewTab(uri: Uri, tabId: String) {
        browserHeaderViewModel?.resetContent()
        if (webViewReplacementUseCase(tabId)) {
            loadUrl(uri.toString())
        }
    }

    private fun loadUrl(url: String) {
        if (url.isEmpty()) {
            return
        }

        val currentView = currentView() ?: return

        if (currentView.url.isNullOrEmpty()
                && Urls.isValidUrl(url)
                && NetworkChecker.isNotAvailable(context)
        ) {
            autoArchive.load(currentView, idGenerator.from(url)) {
                contentViewModel?.snackShort("Load archive.")
            }
            return
        }

        if (preferenceApplier.wifiOnly && NetworkChecker.isUnavailableWiFi(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }

        currentView.loadUrl(url)
    }

    private fun updateBackButtonState(newState: Boolean) {
        browserHeaderViewModel?.setBackButtonIsEnabled(newState)
    }

    private fun updateForwardButtonState(newState: Boolean) {
        browserHeaderViewModel?.setForwardButtonIsEnabled(newState)
    }

    /**
     * Simple delegation to [WebView].
     */
    fun reload() {
        if (preferenceApplier.wifiOnly && NetworkChecker.isUnavailableWiFi(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }
        currentView()?.reload()
    }

    fun find(keyword: String?) {
        keyword ?: return
        currentView()?.findAllAsync(keyword)
    }

    fun findUp() {
        currentView()?.findNext(false)
    }

    fun findDown() {
        currentView()?.findNext(true)
    }

    fun pageUp() {
        currentView()?.pageUp(true)
    }

    fun pageDown() {
        currentView()?.pageDown(true)
    }

    fun back() = currentView()?.let {
        return if (it.canGoBack()) {
            it.goBack()
            updateBackButtonState(it.canGoBack())
            updateForwardButtonState(it.canGoForward())
            true
        } else false
    } ?: false

    fun forward() = currentView()?.let {
        if (it.canGoForward()) {
            it.goForward()
        }

        updateBackButtonState(it.canGoBack())
        updateForwardButtonState(it.canGoForward())
    }

    /**
     * Save archive file.
     */
    fun saveArchive() {
        val currentView = currentView() ?: return
        Archive.save(currentView)
    }

    /**
     * Save archive file.
     */
    fun saveArchiveForAutoArchive() {
        val webView = currentView()
        autoArchive.save(webView, idGenerator.from(webView?.url))
    }

    fun resetUserAgent(userAgentText: String) {
        try {
            currentView()?.settings?.userAgentString = userAgentText
            reload()
        } catch (e: RuntimeException) {
            Timber.e(e)
        }
    }

    /**
     * Is disable Pull-to-Refresh?
     *
     * @return is disable Pull-to-Refresh
     */
    fun disablePullToRefresh(): Boolean =
            (currentView() as? CustomWebView)?.let { !it.enablePullToRefresh || it.scrollY != 0 } ?: false

    /**
     * Stop loading in current tab.
     */
    fun stopLoading() {
        currentView()?.stopLoading()
    }

    fun onResume() {
        GlobalWebViewPool.onResume()
    }

    fun onPause() {
        GlobalWebViewPool.storeStates(context)
        GlobalWebViewPool.onPause()
    }

    fun onDestroy() {
        webViewContainer?.removeAllViews()
    }

    /**
     * Return current [WebView].
     *
     * @return [WebView]
     */
    private fun currentView(): WebView? {
        return GlobalWebViewPool.getLatest()
    }

    /**
     * Return current [WebView]'s URL.
     *
     * @return URL string (Nullable)
     */
    fun currentUrl(): String? = currentView()?.url

    /**
     * Return current [WebView]'s title.
     *
     * @return title (NonNull)
     */
    fun currentTitle(): String = currentView()?.title ?: ""

    fun onSaveInstanceState(outState: Bundle) {
        currentView()?.saveState(outState)
    }

    fun makeCurrentPageInformation(): Bundle = PageInformationExtractor().invoke(currentView())

    /**
     * Resize [GlobalWebViewPool].
     *
     * @param poolSize
     */
    fun resizePool(poolSize: Int) {
        GlobalWebViewPool.resize(poolSize)
    }

    fun applyNewAlpha() {
        GlobalWebViewPool.applyNewAlpha(alphaConverter.readBackground(context))
    }

    fun makeShareMessage() = "${currentTitle()}${System.lineSeparator()}${currentUrl()}"

    fun invokeContentExtraction(callback: ValueCallback<String>) {
        readerModeUseCase(currentView(), callback)
    }

    fun invokeHtmlSourceExtraction(callback: ValueCallback<String>) {
        HtmlSourceExtractionUseCase()(currentView(), callback)
    }

    fun downloadAllImages() {
        AllImageDownloaderUseCase(DownloadAction(context)).invoke(currentView())
    }

}