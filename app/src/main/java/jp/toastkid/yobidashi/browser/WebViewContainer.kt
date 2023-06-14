package jp.toastkid.yobidashi.browser

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.event.Event
import jp.toastkid.lib.viewmodel.event.content.ShareEvent
import jp.toastkid.lib.viewmodel.event.content.ToBottomEvent
import jp.toastkid.lib.viewmodel.event.content.ToTopEvent
import jp.toastkid.lib.viewmodel.event.finder.ClearFinderInputEvent
import jp.toastkid.lib.viewmodel.event.finder.FindAllEvent
import jp.toastkid.lib.viewmodel.event.finder.FindInPageEvent
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
import jp.toastkid.yobidashi.browser.webview.WebViewStateUseCase
import jp.toastkid.yobidashi.browser.webview.factory.WebChromeClientFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewClientFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewLongTapListenerFactory
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @author toastkidjp
 */
class WebViewContainer(
    context: Context,
    private var browserViewModel: BrowserViewModel,
    private val coroutineScope: CoroutineScope
) {
    private val webViewContainer = FrameLayout(context)

    private val context = webViewContainer.context

    private val preferenceApplier = PreferenceApplier(context)

    private val faviconApplier: FaviconApplier = FaviconApplier(context)

    private val readerModeUseCase by lazy { ReaderModeUseCase() }

    private var customViewSwitcher: CustomViewSwitcher? = null

    private val autoArchive = AutoArchive.make(context)

    private val webViewReplacementUseCase: WebViewReplacementUseCase

    private val idGenerator = IdGenerator()

    private var contentViewModel: ContentViewModel? = null

    private val webViewFactory: WebViewFactory

    private val alphaConverter = AlphaConverter()

    private val networkChecker = NetworkChecker()

    private val webViewClient: WebViewClient

    private val webChromeClient: WebChromeClient

    private val longTapListener = WebViewLongTapListenerFactory().invoke { title, url, imageUrl ->
        browserViewModel.setLongTapParameters(title, url, imageUrl)
        browserViewModel.openLongTapDialog.value = true
    }

    private val nestedScrollDispatcher = NestedScrollDispatcher()

    private val scrollListener =
        View.OnScrollChangeListener { _, scrollX, scrollY, oldScrollX, oldScrollY ->
            nestedScrollDispatcher.dispatchPreScroll(
                Offset((oldScrollX - scrollX).toFloat(), (oldScrollY - scrollY).toFloat()),
                NestedScrollSource.Fling
            )
            browserViewModel.swipeRefreshState.value?.isSwipeInProgress = false
            coroutineScope.launch {
                browserViewModel.swipeRefreshState.value?.resetOffset()
            }
        }

    init {
        webViewContainer.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        customViewSwitcher = CustomViewSwitcher({ context }, { currentView() })

        if (context is ComponentActivity) {
            val viewModelProvider = ViewModelProvider(context)
            contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
        }

        webViewFactory = WebViewFactory()

        webViewClient = WebViewClientFactory(
            contentViewModel,
            AdRemover.make(context.assets),
            faviconApplier,
            preferenceApplier,
            browserViewModel,
            RssAddingSuggestion(preferenceApplier),
            { GlobalWebViewPool.getLatest() }
        ).invoke()

        webChromeClient = WebChromeClientFactory(
            browserViewModel,
            faviconApplier,
            CustomViewSwitcher({ context }, { GlobalWebViewPool.getLatest() })
        ).invoke()

        webViewReplacementUseCase = WebViewReplacementUseCase(
            webViewContainer,
            WebViewStateUseCase.make(context),
            { webViewFactory.make(context) },
            browserViewModel,
            preferenceApplier
        )
    }

    fun loadWithNewTab(uri: Uri, tabId: String) {
        val replaced = webViewReplacementUseCase(tabId)

        val latest = GlobalWebViewPool.getLatest()
        latest?.setOnScrollChangeListener(scrollListener)
        latest?.webViewClient = webViewClient
        latest?.webChromeClient = webChromeClient
        latest?.setOnLongClickListener(longTapListener)
        (latest as? CustomWebView)?.setNestedScrollDispatcher(nestedScrollDispatcher)

        if (replaced) {
            loadUrl(uri.toString())
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                faviconApplier.load(uri)?.let {
                    browserViewModel.newIcon(it)
                }
            }
        }
    }

    private fun loadUrl(url: String) {
        if (url.isEmpty()) {
            return
        }

        val currentView = currentView() ?: return

        if (currentView.url.isNullOrEmpty()
                && Urls.isValidUrl(url)
                && networkChecker.isNotAvailable(context)
        ) {
            autoArchive.load(currentView, idGenerator.from(url)) {
                contentViewModel?.snackShort("Load archive.")
            }
            return
        }

        if (preferenceApplier.wifiOnly && networkChecker.isUnavailableWiFi(context)) {
            contentViewModel?.snackShort(R.string.message_wifi_not_connecting)
            return
        }

        currentView.loadUrl(url)
    }

    private fun updateBackButtonState(newState: Boolean) {
        browserViewModel?.setBackButtonIsEnabled(newState)
    }

    private fun updateForwardButtonState(newState: Boolean) {
        browserViewModel?.setForwardButtonIsEnabled(newState)
    }

    /**
     * Simple delegation to [WebView].
     */
    fun reload() {
        if (preferenceApplier.wifiOnly && networkChecker.isUnavailableWiFi(context)) {
            contentViewModel?.snackShort(R.string.message_wifi_not_connecting)
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

    fun clearMatches() {
        currentView()?.clearMatches()
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
     * Stop loading in current tab.
     */
    fun stopLoading() {
        currentView()?.stopLoading()
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
    private fun resizePool(poolSize: Int) {
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

    fun refresh() {
        applyNewAlpha()
        val preferenceApplier = PreferenceApplier(context)
        resizePool(preferenceApplier.poolSize)
    }

    fun useEvent(event: Event) {
        when (event) {
            is ToTopEvent -> {
                pageUp()
            }
            is ToBottomEvent -> {
                pageDown()
            }
            is ShareEvent -> {
                context.startActivity(
                    ShareIntentFactory()(makeShareMessage())
                )
            }
            is FindAllEvent -> {
                find(event.word)
            }
            is FindInPageEvent -> {
                if (event.upward) {
                    findUp()
                } else {
                    findDown()
                }
            }
            is ClearFinderInputEvent -> {
                clearMatches()
            }
            else -> Unit
        }
    }

    fun view() = webViewContainer

    fun nestedScrollDispatcher() = nestedScrollDispatcher

}