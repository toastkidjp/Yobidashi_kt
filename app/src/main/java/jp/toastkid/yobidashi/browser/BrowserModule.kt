package jp.toastkid.yobidashi.browser

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.Archive
import jp.toastkid.yobidashi.browser.archive.IdGenerator
import jp.toastkid.yobidashi.browser.archive.auto.AutoArchive
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.yobidashi.browser.history.ViewHistoryInsertion
import jp.toastkid.yobidashi.browser.reader.ReaderModeUseCase
import jp.toastkid.yobidashi.browser.user_agent.UserAgent
import jp.toastkid.yobidashi.browser.webview.CustomViewSwitcher
import jp.toastkid.yobidashi.browser.webview.CustomWebView
import jp.toastkid.yobidashi.browser.webview.WebViewPool
import jp.toastkid.yobidashi.libs.BitmapCompressor
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.libs.network.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.main.content.ContentViewModel
import jp.toastkid.yobidashi.rss.suggestion.RssAddingSuggestion
import jp.toastkid.yobidashi.tab.History
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * @author toastkidjp
 */
class BrowserModule(
        private val context: Context,
        private val webViewContainer: FrameLayout?
) {

    private val webViewPool: WebViewPool

    private val preferenceApplier = PreferenceApplier(context)

    private val rssAddingSuggestion = RssAddingSuggestion(preferenceApplier)

    private val faviconApplier: FaviconApplier = FaviconApplier(context)

    private val readerModeUseCase by lazy { ReaderModeUseCase() }

    private val htmlSourceExtractionUseCase by lazy { HtmlSourceExtractionUseCase() }

    /**
     * Loading flag.
     */
    private var isLoadFinished: Boolean = false

    private var customViewSwitcher: CustomViewSwitcher? = null

    private val adRemover: AdRemover =
            AdRemover(context.assets.open("ad_hosts.txt"))

    private val autoArchive = AutoArchive.make(context)

    private var headerViewModel: HeaderViewModel? = null

    private var browserHeaderViewModel: BrowserHeaderViewModel? = null

    private var loadingViewModel: LoadingViewModel? = null

    /**
     * Animation of slide up bottom.
     */
    private val slideUpFromBottom
            = AnimationUtils.loadAnimation(context, R.anim.slide_up)

    private val slideDown
            = AnimationUtils.loadAnimation(context, R.anim.slide_down)

    private val idGenerator = IdGenerator()

    private var lastId = ""

    private var contentViewModel: ContentViewModel? = null

    private val disposables = CompositeDisposable()

    init {
        webViewPool = WebViewPool(
                context,
                { makeWebViewClient() },
                { makeWebChromeClient() },
                preferenceApplier.poolSize
        )

        customViewSwitcher = CustomViewSwitcher({ context }, { currentView() })

        if (context is MainActivity) {
            val viewModelProvider = ViewModelProviders.of(context)
            headerViewModel = viewModelProvider.get(HeaderViewModel::class.java)
            browserHeaderViewModel = viewModelProvider.get(BrowserHeaderViewModel::class.java)
            loadingViewModel = viewModelProvider.get(LoadingViewModel::class.java)
            contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
        }
    }

    private fun makeWebViewClient(): WebViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            headerViewModel?.updateProgress(0)
            isLoadFinished = false

            rssAddingSuggestion(view, url).addTo(disposables)
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            isLoadFinished = true

            loadingViewModel?.finished(lastId, History.make(view.title, view.url))

            headerViewModel?.updateProgress(100)
            headerViewModel?.stopProgress(true)

            val title = view.title ?: ""
            val urlStr = url ?: ""

            try {
                if (view == currentView()) {
                    browserHeaderViewModel?.nextTitle(title)
                    browserHeaderViewModel?.nextUrl(urlStr)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

            if (preferenceApplier.saveViewHistory
                    && title.isNotEmpty()
                    && urlStr.isNotEmpty()
            ) {
                ViewHistoryInsertion
                        .make(
                                view.context,
                                title,
                                urlStr,
                                faviconApplier.makePath(urlStr)
                        )
                        .invoke()
            }

            if (preferenceApplier.useInversion) {
                InversionScript()(view)
            }
        }

        override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            headerViewModel?.updateProgress(100)
            headerViewModel?.stopProgress(true)
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            super.onReceivedSslError(view, handler, error)

            handler?.cancel()

            if (context !is FragmentActivity || context.isFinishing) {
                return
            }

            TlsErrorDialogFragment
                    .make(TlsErrorMessageGenerator().invoke(context, error))
                    .show(
                            context.supportFragmentManager,
                            TlsErrorDialogFragment::class.java.simpleName
                    )
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                if (preferenceApplier.adRemove) {
                    adRemover(request.url.toString())
                } else {
                    super.shouldInterceptRequest(view, request)
                }

        @Suppress("OverridingDeprecatedMember")
        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? =
                if (preferenceApplier.adRemove) {
                    adRemover(url)
                } else {
                    @Suppress("DEPRECATION")
                    super.shouldInterceptRequest(view, url)
                }

        @Suppress("DEPRECATION")
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
                shouldOverrideUrlLoading(view, request?.url?.toString())

        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
                url?.let {
                    val context: Context? = view?.context
                    val uri: Uri = Uri.parse(url)
                    when (uri.scheme) {
                        "market", "intent" -> {
                            try {
                                context?.startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME))
                                true
                            } catch (e: ActivityNotFoundException) {
                                Timber.w(e)

                                context?.let {
                                    contentViewModel?.snackShort(context.getString(R.string.message_cannot_launch_app))
                                }
                                true
                            }
                        }
                        "tel" -> {
                            context?.startActivity(IntentFactory.dial(uri))
                            view?.reload()
                            true
                        }
                        "mailto" -> {
                            context?.startActivity(IntentFactory.mailTo(uri))
                            view?.reload()
                            true
                        }
                        else -> {
                            super.shouldOverrideUrlLoading(view, url)
                        }
                    }
                } ?: super.shouldOverrideUrlLoading(view, url)
    }

    private fun makeWebChromeClient(): WebChromeClient = object : WebChromeClient() {

        private val bitmapCompressor = BitmapCompressor()

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

            headerViewModel?.updateProgress(newProgress)
            headerViewModel?.stopProgress(newProgress < 65)

            if (isLoadFinished) {
                return
            }

            try {
                val progressTitle =
                        view.context.getString(R.string.prefix_loading) + newProgress + "%"
                browserHeaderViewModel?.nextTitle(progressTitle)
                browserHeaderViewModel?.nextUrl(view.url)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        override fun onReceivedIcon(view: WebView?, favicon: Bitmap?) {
            super.onReceivedIcon(view, favicon)
            if (view?.url != null && favicon != null) {
                bitmapCompressor(favicon, faviconApplier.assignFile(view.url))
            }
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            super.onShowCustomView(view, callback)
            customViewSwitcher?.onShowCustomView(view, callback)
        }

        override fun onHideCustomView() {
            super.onHideCustomView()
            customViewSwitcher?.onHideCustomView()
        }

        override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
        ): Boolean {
            val href = view?.handler?.obtainMessage()
            view?.requestFocusNodeHref(href)
            val url = href?.data?.getString("url")?.toUri()
                    ?: return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
            view.stopLoading()

            (context as? FragmentActivity)?.also { fragmentActivity ->
                ViewModelProviders.of(fragmentActivity)
                        .get(BrowserViewModel::class.java)
                        .open(url)
            }

            return true
        }
    }

    fun loadWithNewTab(uri: Uri, tabId: String) {
        lastId = tabId
        if (replaceWebView(tabId)) {
            loadUrl(uri.toString())
        }
    }

    private fun loadUrl(url: String) {
        if (url.isEmpty()) {
            return
        }

        val context: Context = context

        val currentView = currentView()
        if (TextUtils.isEmpty(currentUrl())
                && Urls.isValidUrl(url)
                && NetworkChecker.isNotAvailable(context)
        ) {
            currentView ?: return
            autoArchive.load(currentView, idGenerator.from(url)) {
                contentViewModel?.snackShort("Load archive.")
            }
            return
        }

        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }

        currentView?.loadUrl(url)
    }

    private fun replaceWebView(tabId: String): Boolean {
        browserHeaderViewModel?.resetContent()

        val currentWebView = getWebView(tabId)
        if (webViewContainer?.childCount != 0) {
            val previousView = webViewContainer?.get(0)
            if (currentWebView == previousView) {
                return false
            }
        }

        webViewContainer?.removeAllViews()
        currentWebView?.let {
            it.onResume()
            webViewContainer?.addView(it)
            updateForwardButtonState(it.canGoForward())
            browserHeaderViewModel?.nextTitle(it.title)
            browserHeaderViewModel?.nextUrl(it.url)
            it.startAnimation(slideUpFromBottom)

            val activity = webViewContainer?.context
            if (activity is FragmentActivity
                    && preferenceApplier.browserScreenMode() != ScreenMode.FULL_SCREEN) {
                ViewModelProviders.of(activity).get(HeaderViewModel::class.java).show()
            }
        }

        reloadWebViewSettings().addTo(disposables)
        return currentWebView?.url.isNullOrBlank()
    }

    private fun updateForwardButtonState(newState: Boolean) {
        browserHeaderViewModel?.setForwardButtonEnability(newState)
    }

    /**
     * Simple delegation to [WebView].
     */
    fun reload() {
        if (preferenceApplier.wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }
        currentView()?.reload()
    }

    fun animate(animation: Animation?) {
        animation?.let { currentView()?.startAnimation(it) }
    }

    fun find(keyword: String?) {
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
            updateForwardButtonState(it.canGoForward())
            true
        } else false
    } ?: false

    fun forward() = currentView()?.let {
        if (it.canGoForward()) {
            it.goForward()
        }

        updateForwardButtonState(it.canGoForward())
    }

    fun canCurrentTabGoesBack() = currentView()?.canGoBack() == true

    /**
     * Save archive file.
     */
    fun saveArchive() {
        val currentView = currentView() ?: return
        if (Archive.cannotUseArchive()) {
            contentViewModel?.snackShort(currentView.context.getString(R.string.message_disable_archive))
            return
        }
        Archive.save(currentView)
    }

    /**
     * Save archive file.
     */
    fun saveArchiveForAutoArchive() {
        if (Archive.cannotUseArchive()) {
            return
        }

        val webView = currentView()
        autoArchive.save(webView, idGenerator.from(webView?.url))
    }

    /**
     * Reload [WebSettings].
     *
     * @return subscription
     */
    fun reloadWebViewSettings(): Disposable {
        val settings = currentView()?.settings?.also {
            it.javaScriptEnabled = preferenceApplier.useJavaScript()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                it.saveFormData = preferenceApplier.doesSaveForm()
            }
            it.loadsImagesAutomatically = preferenceApplier.doesLoadImage()
        }

        return Single.fromCallable { preferenceApplier.userAgent() }
                .map { uaName ->
                    val text = UserAgent.valueOf(uaName).text()

                    if (text.isNotEmpty()) {
                        text
                    } else {
                        WebView(context).settings.userAgentString
                    }
                }
                .filter { settings?.userAgentString != it }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { resetUserAgent(it) }
    }

    fun resetUserAgent(userAgentText: String) {
        currentView()?.settings?.userAgentString = userAgentText
        reload()
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
        currentView()?.onResume()
    }

    fun onPause() {
        currentView()?.onPause()
    }

    /**
     * Dispose [WebViewPool].
     */
    fun dispose() {
        webViewPool.dispose()
        disposables.clear()
    }

    /**
     * Disable [WebView].
     */
    fun disableWebView() {
        currentView()?.let {
            it.isEnabled = false
            it.visibility = View.GONE
        }
        stopLoading()
    }

    /**
     * Return current [WebView].
     *
     * @return [WebView]
     */
    private fun currentView(): WebView? = webViewPool.getLatest()

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

    /**
     * Get [WebView] with tab ID.
     *
     * @param tabId Tab's ID.
     * @return [WebView]
     */
    fun getWebView(tabId: String?): WebView? = webViewPool.get(tabId)

    /**
     * Detach [WebView] with tab ID.
     *
     * @param tabId Tab's ID.
     */
    fun detachWebView(tabId: String?) = webViewPool.remove(tabId)

    fun onSaveInstanceState(outState: Bundle) {
        currentView()?.saveState(outState)
    }

    fun onViewStateRestored(savedInstanceState: Bundle?) {
        currentView()?.restoreState(savedInstanceState)
    }

    fun makeCurrentPageInformation(): Bundle = Bundle().also { bundle ->
        return currentView()?.let {
            bundle.putParcelable("favicon", it.favicon)
            bundle.putString("title", it.title)
            bundle.putString("url", it.url)
            bundle
        } ?: bundleOf()
    }

    /**
     * Resize [WebViewPool].
     *
     * @param poolSize
     */
    fun resizePool(poolSize: Int) {
        webViewPool.resize(poolSize)
    }

    fun applyNewAlpha() {
        webViewPool.applyNewAlpha()
    }

    fun makeShareMessage() = "${currentTitle()}$lineSeparator${currentUrl()}"

    fun invokeContentExtraction(callback: ValueCallback<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            readerModeUseCase(currentView(), callback)
        }
    }

    fun invokeHtmlSourceExtraction(callback: ValueCallback<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            htmlSourceExtractionUseCase(currentView(), callback)
        }
    }

    @Throws(IOException::class)
    fun loadArchive(archiveFile: File) {
        currentView()?.let { Archive.loadArchive(it, archiveFile) }
    }

    companion object {
        private val lineSeparator = System.getProperty("line.separator")
    }
}