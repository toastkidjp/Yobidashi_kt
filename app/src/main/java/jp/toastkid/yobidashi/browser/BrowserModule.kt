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
import android.view.View
import android.view.animation.Animation
import android.webkit.*
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.Archive
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.yobidashi.browser.history.ViewHistoryInsertion
import jp.toastkid.yobidashi.browser.screenshots.Screenshot
import jp.toastkid.yobidashi.browser.user_agent.UserAgent
import jp.toastkid.yobidashi.browser.webview.CustomViewSwitcher
import jp.toastkid.yobidashi.browser.webview.CustomWebView
import jp.toastkid.yobidashi.browser.webview.StateRepository
import jp.toastkid.yobidashi.browser.webview.WebViewPool
import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.main.MainActivity
import timber.log.Timber

/**
 * @author toastkidjp
 */
class BrowserModule(
        private val context: Context,
        private val historyAddingCallback: (String, String) -> Unit
) {

    private val webViewPool: WebViewPool

    private val preferenceApplier = PreferenceApplier(context)

    private val faviconApplier: FaviconApplier = FaviconApplier(context)

    /**
     * Loading flag.
     */
    private var isLoadFinished: Boolean = false

    private var customViewSwitcher: CustomViewSwitcher? = null

    private val adRemover: AdRemover =
            AdRemover(context.assets.open("ad_hosts.txt"))

    private var headerViewModel: HeaderViewModel? = null

    private var browserHeaderViewModel: BrowserHeaderViewModel? = null

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
        }
    }

    private fun makeWebViewClient(): WebViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            headerViewModel?.updateProgress(0)
            isLoadFinished = false
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            isLoadFinished = true
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

            historyAddingCallback(title, urlStr)

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
                        .insert()
            }

            if (preferenceApplier.useInversion) {
                InversionScript(view)
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
                    .make(SslErrorMessageGenerator.generate(context, error))
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
                            } catch (e: ActivityNotFoundException) {
                                Timber.w(e)
                                view?.let {
                                    Toaster.snackShort(it,
                                            R.string.message_cannot_launch_app,
                                            preferenceApplier.colorPair()
                                    )
                                }
                            }
                            true
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
                Bitmaps.compress(favicon, faviconApplier.assignFile(view.url))
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
            context.startActivity(MainActivity.makeBrowserIntent(context, url))
            return true
        }
    }

    fun loadUrl(url: String) {
        if (url.isEmpty()) {
            return
        }
        val context: Context = context
        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            //return
        }

        currentView()?.loadUrl(url)
    }

    fun findAllAsync(text: String) {
        currentView()?.findAllAsync(text)
    }

    fun makeDrawingCache(): Bitmap? = currentView()?.let {
        it.invalidate()
        it.buildDrawingCache()
        it.drawingCache
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

    fun animate(slideUpFromBottom: Animation?) {
        slideUpFromBottom?.let { currentView()?.startAnimation(it) }
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
            true
        } else false
    } ?: false

    fun forward() = currentView()?.let {
        if (it.canGoForward()) {
            it.goForward()
        }
    }

    fun canCurrentTabGoesBack() = currentView()?.canGoBack() == true

    /**
     * Save archive file.
     */
    fun saveArchive() {
        val currentView = currentView() ?: return
        if (Archive.cannotUseArchive()) {
            Toaster.snackShort(
                    currentView,
                    R.string.message_disable_archive,
                    preferenceApplier.colorPair()
            )
            return
        }
        Archive.save(currentView)
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

    fun currentSnap() {
        currentView()?.let {
            it.invalidate()
            it.buildDrawingCache()
            Screenshot.save(it.context, it.drawingCache)
        }
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
            currentView()?.let { !(it as CustomWebView).enablePullToRefresh || it.scrollY != 0 } ?: false

    /**
     * Stop loading in current tab.
     */
    fun stopLoading() {
        currentView()?.stopLoading()
    }

    /**
     * Dispose [WebViewPool].
     */
    fun dispose() {
        webViewPool.dispose()
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

    fun onSaveInstanceState(id: String) {
        val webView = currentView() ?: return
        StateRepository(context.filesDir).save(id, webView)
    }

    fun onViewStateRestored(id: String) {
        val webView = currentView() ?: return
        StateRepository(context.filesDir).load(id, webView)
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

}