package jp.toastkid.web

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
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.network.DownloadAction
import jp.toastkid.lib.network.NetworkChecker
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.event.Event
import jp.toastkid.lib.viewmodel.event.content.ShareEvent
import jp.toastkid.lib.viewmodel.event.content.ToBottomEvent
import jp.toastkid.lib.viewmodel.event.content.ToTopEvent
import jp.toastkid.lib.viewmodel.event.finder.ClearFinderInputEvent
import jp.toastkid.lib.viewmodel.event.finder.FindAllEvent
import jp.toastkid.lib.viewmodel.event.finder.FindInPageEvent
import jp.toastkid.web.archive.Archive
import jp.toastkid.web.archive.IdGenerator
import jp.toastkid.web.archive.auto.AutoArchive
import jp.toastkid.web.block.AdRemover
import jp.toastkid.web.download.image.AllImageDownloaderUseCase
import jp.toastkid.web.page_information.PageInformationExtractor
import jp.toastkid.web.reader.ReaderModeUseCase
import jp.toastkid.web.rss.suggestion.RssAddingSuggestion
import jp.toastkid.web.usecase.HtmlSourceExtractionUseCase
import jp.toastkid.web.usecase.WebViewReplacementUseCase
import jp.toastkid.web.view.WebTabUiViewModel
import jp.toastkid.web.view.component.SeekEvent
import jp.toastkid.web.webview.AlphaConverter
import jp.toastkid.web.webview.CustomViewSwitcher
import jp.toastkid.web.webview.CustomWebView
import jp.toastkid.web.webview.GlobalWebViewPool
import jp.toastkid.web.webview.WebViewStateUseCase
import jp.toastkid.web.webview.factory.WebChromeClientFactory
import jp.toastkid.web.webview.factory.WebViewClientFactory
import jp.toastkid.web.webview.factory.WebViewFactory
import jp.toastkid.web.webview.factory.WebViewLongTapListenerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @author toastkidjp
 */
class WebViewContainer(
    private val context: Context,
    private var browserViewModel: WebTabUiViewModel
) {
    private val webViewContainer = FrameLayout(context)

    private val preferenceApplier = PreferenceApplier(context)

    private val faviconApplier: FaviconApplier = FaviconApplier(context)

    private val readerModeUseCase by lazy { ReaderModeUseCase() }

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
    }

    private val nestedScrollDispatcher = NestedScrollDispatcher()

    private val scrollListener =
        View.OnScrollChangeListener { _, scrollX, scrollY, oldScrollX, oldScrollY ->
            nestedScrollDispatcher.dispatchPreScroll(
                Offset((oldScrollX - scrollX).toFloat(), (oldScrollY - scrollY).toFloat()),
                NestedScrollSource.SideEffect
            )
        }

    init {
        webViewContainer.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

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
            AutoArchive.make(context),
            browserViewModel,
            RssAddingSuggestion(preferenceApplier),
            GlobalWebViewPool::getLatest
        ).invoke()

        webChromeClient = WebChromeClientFactory(
            browserViewModel,
            faviconApplier,
            CustomViewSwitcher({ context }, GlobalWebViewPool::getLatest)
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
                faviconApplier.load(uri)?.let(browserViewModel::newIcon)
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
                contentViewModel?.snackShort(it)
            }
            return
        }

        if (preferenceApplier.wifiOnly && networkChecker.isUnavailableWiFi(context) && !url.startsWith("file://")) {
            contentViewModel?.snackShort(jp.toastkid.lib.R.string.message_wifi_not_connecting)
            return
        }

        currentView.loadUrl(url)
    }

    private fun updateBackButtonState(newState: Boolean) {
        browserViewModel.setBackButtonIsEnabled(newState)
    }

    private fun updateForwardButtonState(newState: Boolean) {
        browserViewModel.setForwardButtonIsEnabled(newState)
    }

    /**
     * Simple delegation to [WebView].
     */
    fun reload() {
        if (preferenceApplier.wifiOnly && networkChecker.isUnavailableWiFi(context)) {
            contentViewModel?.snackShort(jp.toastkid.lib.R.string.message_wifi_not_connecting)
            return
        }
        currentView()?.reload()
    }

    fun find(keyword: String?) {
        keyword ?: return
        currentView()?.findAllAsync(keyword)
    }

    private fun findUp() {
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

    fun makeCurrentPageInformation() = PageInformationExtractor().invoke(currentView())

    /**
     * Resize [GlobalWebViewPool].
     *
     * @param poolSize
     */
    private fun resizePool(poolSize: Int) {
        GlobalWebViewPool.resize(poolSize)
    }

    private fun applyNewAlpha() {
        GlobalWebViewPool.applyNewAlpha(alphaConverter.readBackground(context))
    }

    private fun makeShareMessage() = "${currentTitle()}${System.lineSeparator()}${currentUrl()}"

    fun invokeContentExtraction(callback: ValueCallback<String>) {
        readerModeUseCase(currentView(), callback)
    }

    fun invokeHtmlSourceExtraction(callback: ValueCallback<String>) {
        HtmlSourceExtractionUseCase()(currentView(), callback)
    }

    fun downloadAllImages() {
        if (preferenceApplier.wifiOnly && networkChecker.isUnavailableWiFi(context)) {
            contentViewModel?.snackShort(jp.toastkid.lib.R.string.message_wifi_not_connecting)
            return
        }
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

    fun seek(seekEvent: SeekEvent) {
        val webView = currentView() ?: return

        val offsetFromLiveEdge = seekEvent.toOffsetSeconds()
        val jsCode = """
(function() {
            var player = videojs.getPlayer(document.querySelector('.video-js'));
            if (!player && typeof videojs !== 'undefined' && videojs.getPlayers) {
                var players = videojs.getPlayers();
                var keys = Object.keys(players);
                if (keys.length > 0) {
                    player = players[keys[0]];
                }
            }
            
            // seekable レンジを取得（Video.js または HTML5 videoタグ）
            var seekableRange = null;
            if (player && typeof player.seekable === 'function') {
                seekableRange = player.seekable();
            } else {
                var videoTag = document.querySelector('video');
                if (videoTag && videoTag.seekable) {
                    seekableRange = videoTag.seekable;
                }
            }
            
            if (!seekableRange || seekableRange.length === 0) {
                return 'ERROR: NO_SEEKABLE_RANGE';
            }
            
            // ストリーミングのシーク可能範囲の端を取得
            var startTime = seekableRange.start(0); // シーク可能な最も古い時間（バッファの起点）
            var endTime = seekableRange.end(0);     // シーク可能な最も新しい時間（現在のライブ端）
            
            // ライブ端から指定秒数巻き戻した位置を算出
            var targetTime = endTime - $offsetFromLiveEdge;
            
            // 範囲内にクランプ（起点より古くならないようにする）
            if (targetTime < startTime) {
                targetTime = startTime;
            }
            
            // シークを実行
            if (player) {
                player.currentTime(targetTime);
            } else {
                document.querySelector('video').currentTime = targetTime;
            }
            
            return 'SUCCESS_SEEK: target=' + targetTime + ' (range: ' + startTime + ' ~ ' + endTime + ')';
        })();
    """.trimIndent()

        webView.evaluateJavascript(jsCode) {
            Timber.d("Player: $it")
        }
    }

}