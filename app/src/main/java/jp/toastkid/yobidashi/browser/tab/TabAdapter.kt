package jp.toastkid.yobidashi.browser.tab

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.TitlePair
import jp.toastkid.yobidashi.browser.UserAgent
import jp.toastkid.yobidashi.browser.WebViewFactory
import jp.toastkid.yobidashi.browser.archive.Archive
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.history.ViewHistoryInsertion
import jp.toastkid.yobidashi.browser.screenshots.Screenshot
import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.Storeroom
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SiteSearch
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection


/**
 * ModuleAdapter of [Tab].
 *
 * @author toastkidjp
 */
class TabAdapter(
        progress: ProgressBar,
        webViewContainer: FrameLayout,
        private val tabCount: TextView,
        titleCallback: (TitlePair) -> Unit,
        private val loadedCallback: () -> Unit,
        touchCallback: () -> Unit,
        private val scrollCallback: (Boolean) -> Unit,
        private val tabEmptyCallback: () -> Unit
) {

    private val tabList: TabList = TabList.loadOrInit(progress.context)

    private val colorPair: ColorPair

    private val webView: CustomWebView

    /** Loading flag.  */
    private var isLoadFinished: Boolean = false

    private var backOrForwardProgress: Boolean = false

    private val tabsScreenshots: Storeroom

    private val faviconApplier: FaviconApplier = FaviconApplier(progress.context)

    private val preferenceApplier: PreferenceApplier

    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * Animation of slide in left.
     */
    private val slideInLeft
            = AnimationUtils.loadAnimation(tabCount.context, android.R.anim.slide_in_left)

    /**
     * Animation of slide in right.
     */
    private val slideInRight
            = AnimationUtils.loadAnimation(tabCount.context, R.anim.slide_in_right)

    /**
     * Animation of slide up bottom.
     */
    private val slideUpBottom
            = AnimationUtils.loadAnimation(tabCount.context, R.anim.slide_up)

    /**
     * Suppressing unnecessary animation.
     */
    private val minimumScrolled: Int = 15

    init {
        webView = makeWebView(progress, titleCallback, touchCallback)
        webViewContainer.addView(this.webView)

        tabsScreenshots = Storeroom(webView.context, "tabs/screenshots")
        preferenceApplier = PreferenceApplier(webView.context)
        colorPair = preferenceApplier.colorPair()
        setCurrentTabCount()
    }

    private fun makeWebView(
            progress: ProgressBar,
            titleCallback: (TitlePair) -> Unit,
            touchCallback: () -> Unit
    ): CustomWebView {
        val webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progress.visibility = View.VISIBLE
                isLoadFinished = false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                isLoadFinished = true
                progress.visibility = View.GONE

                val lastScrolled = currentTab().latest.scrolled
                if (lastScrolled != 0) {
                    webView.scrollTo(0, lastScrolled)
                }

                val title  = view.title ?: ""
                val urlstr = url ?: ""

                try {
                    titleCallback(TitlePair.make(title, urlstr))
                } catch (e: Exception) {
                    Timber.e(e)
                }

                deleteThumbnail(tabList.currentTab().thumbnailPath)

                saveNewThumbnail()

                if (!backOrForwardProgress) {
                    addHistory(title, urlstr)

                    if (preferenceApplier.saveViewHistory
                            && title.isNotEmpty()
                            && urlstr.isNotEmpty()
                            ) {
                        ViewHistoryInsertion
                                .make(
                                        view.context,
                                        title,
                                        urlstr,
                                        faviconApplier.makePath(urlstr)
                                )
                                .insert()
                    }
                }
                backOrForwardProgress = false
                loadedCallback()
                tabList.save()
            }

            override fun onReceivedError(
                    view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                backOrForwardProgress = false
                loadedCallback()
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                super.onReceivedSslError(view, handler, error)

                handler?.cancel()

                val context = webView.context
                AlertDialog.Builder(context)
                        .setTitle(R.string.title_ssl_connection_error)
                        .setMessage(SslErrorMessageGenerator.generate(context, error))
                        .setPositiveButton(R.string.ok, {d, i -> d.dismiss()})
                        .show()
            }
        }
        val webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (!isLoadFinished) {
                    progress.progress = newProgress
                    try {
                        titleCallback(TitlePair.make(
                                view.context.getString(R.string.prefix_loading) + newProgress + "%",
                                view.url ?: ""
                        )
                        )
                    } catch (e: Exception) {
                        Timber.e(e)
                    }

                }
            }

            override fun onReceivedIcon(view: WebView?, favicon: Bitmap?) {
                super.onReceivedIcon(view, favicon)
                if (view?.url != null && favicon != null) {
                    Bitmaps.compress(favicon, faviconApplier.assignFile(view.url))
                }
            }
        }

        val webView = WebViewFactory.make(progress.context)
        webView.setWebViewClient(webViewClient)
        webView.setWebChromeClient(webChromeClient)
        webView.setOnTouchListener { _, _ ->
            touchCallback()
            false
        }
        webView.setOnLongClickListener { v ->
            val hitResult = webView.hitTestResult
            when (hitResult.type) {
                WebView.HitTestResult.IMAGE_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    AlertDialog.Builder(progress.context)
                            .setTitle("Image: " + url)
                            .setItems(R.array.image_menu, { dialog, which ->
                                when (which) {
                                    0 -> {
                                        disposables.add(
                                                storeImage(url, webView).subscribe{file ->
                                                    preferenceApplier.backgroundImagePath = file.absolutePath
                                                    Toaster.snackShort(
                                                            webView,
                                                            R.string.message_change_background_image,
                                                            preferenceApplier.colorPair()
                                                    )
                                                }
                                        )
                                    }
                                    1 -> disposables.add(storeImage(url, webView).subscribe({
                                        Toaster.snackShort(
                                                webView,
                                                R.string.message_done_save,
                                                preferenceApplier.colorPair()
                                        )
                                    }))
                                    2 -> ImageDownloadAction(webView, hitResult).invoke()
                                }
                            })
                            .setCancelable(true)
                            .setNegativeButton(R.string.cancel, {d, i -> d.cancel()})
                            .show()
                    false
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    AlertDialog.Builder(progress.context)
                            .setTitle("URL: " + url)
                            .setItems(R.array.url_menu, { dialog, which ->
                                when (which) {
                                    0 -> openNewTab(url)
                                    1 -> openBackgroundTab(url)
                                    2 -> loadUrl(url)
                                    3 -> Clipboard.clip(v.context, url)
                                }
                            })
                            .setCancelable(true)
                            .setNegativeButton(R.string.cancel, {d, i -> d.cancel()})
                            .show()
                    false
                }
                else -> {
                    val extra = hitResult.extra
                    if (extra == null || extra.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    AlertDialog.Builder(v.context)
                            .setTitle("Text: " + extra)
                            .setItems(R.array.url_menu, { dialog, which ->
                                when (which) {
                                    0 -> Clipboard.clip(v.context, extra)
                                    1 -> {
                                        SearchAction(
                                                v.context,
                                                preferenceApplier.getDefaultSearchEngine(),
                                                extra
                                        ).invoke()
                                    }
                                }
                            })
                            .setCancelable(true)
                            .setNegativeButton(R.string.cancel, {d, i -> d.cancel()})
                            .show()
                    false
                }
            }
        }
        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setVisibleInDownloadsUi(true)
            request.setMimeType(mimetype)
            request.setDescription(contentDisposition)

            val dm = webView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        }
        webView.scrollListener = { horizontal, vertical, oldHorizontal, oldVertical ->
            val scrolled = vertical - oldVertical
            if (Math.abs(scrolled) > minimumScrolled) {
                scrollCallback(0 > scrolled)
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            WebIconDatabase.getInstance()
                    .open(webView.context.getDir("faviconApplier", Context.MODE_PRIVATE).path);
        }
        return webView
    }

    private fun storeImage(url: String, webView: WebView): Maybe<File> {
        return Single.create<Response> { e ->
            val client = HttpClientFactory.make()
            e.onSuccess(client.newCall(Request.Builder().url(url).build()).execute())
        }.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .filter { it.code() == HttpURLConnection.HTTP_OK }
                .map { BitmapFactory.decodeStream(it.body()?.byteStream()) }
                .map {
                    val storeroom = Storeroom(webView.context, BackgroundSettingActivity.BACKGROUND_DIR)
                    val file = storeroom.assignNewFile(Uri.parse(url))
                    Bitmaps.compress(it, file)
                    file
                }
    }

    private fun saveNewThumbnail() {
        webView.invalidate()
        webView.buildDrawingCache()

        val currentTab = tabList.currentTab()
        val file = tabsScreenshots.assignNewFile("${currentTab.id}.png")
        val drawingCache = webView.drawingCache
        if (drawingCache != null) {
            Bitmaps.compress(drawingCache, file)
        }
        currentTab.thumbnailPath = file.absolutePath
    }

    private fun deleteThumbnail(thumbnailPath: String) {
        if (thumbnailPath.isEmpty()) {
            return
        }

        val lastScreenshot = File(thumbnailPath)
        if (lastScreenshot.exists()) {
            lastScreenshot.delete()
        }
    }

    internal fun openNewTab() {
        openNewTab(preferenceApplier.homeUrl)
    }

    private fun openNewTab(url: String) {
        val newTab = Tab()
        tabList.add(newTab)
        setCurrentTabCount()
        setIndexByTab(newTab, true)
        loadUrl(url)
    }

    private fun openBackgroundTab(url: String) {
        tabList.add(Tab.makeBackground(webView.context.getString(R.string.new_tab), url))
        tabList.save()
        setCurrentTabCount()
    }

    private fun addHistory(title: String, url: String) {
        tabList.currentTab().addHistory(History.make(title, url))
    }

    fun back(): String {
        backOrForwardProgress = true
        return tabList.currentTab().back()
    }

    fun forward(): String {
        backOrForwardProgress = true
        return tabList.currentTab().forward()
    }

    internal fun setIndexByTab(tab: Tab, openNew: Boolean = false) {
        val index = tabList.indexOf(tab)
        updateScrolled()

        if (openNew) {
            webView.startAnimation(slideUpBottom)
            setIndex(index)
            return
        }

        if (index < index()) {
            webView.startAnimation(slideInLeft)
        } else {
            webView.startAnimation(slideInRight)
        }
        setIndex(index)
    }

    private fun setIndex(newIndex: Int) {

        if (checkIndex(newIndex)) {
            return
        }
        tabList.setIndex(newIndex)

        val latest = tabList.currentTab().latest
        if (latest !== History.EMPTY) {
            loadUrl(latest.url())
        }
    }

    private fun checkIndex(newIndex: Int): Boolean = newIndex < 0 || tabList.size() <= newIndex

    fun size(): Int = tabList.size()

    /**
     * Simple delegation to [WebView].
     */
    fun reload() {
        webView.reload()
    }

    /**
     * Reload current tab's latest url.
     */
    fun reloadUrlIfNeed() {
        if (tabList.isEmpty) {
            return
        }
        loadUrl(tabList.currentTab().latest.url())
    }

    fun loadUrl(url: String, saveHistory: Boolean = true) {
        if (url.isEmpty()) {
            return
        }

        if (TextUtils.equals(webView.url, url)) {
            webView.scrollTo(0, currentTab().latest.scrolled)
            return
        }
        backOrForwardProgress = !saveHistory
        webView.loadUrl(url)
    }

    fun pageUp() {
        webView.pageUp(true)
    }

    fun pageDown() {
        webView.pageDown(true)
    }

    fun currentSnap() {
        webView.invalidate()
        webView.buildDrawingCache()
        Screenshot.save(webView.context, webView.drawingCache)
    }

    fun resetUserAgent(userAgentText: String) {
        webView.settings.userAgentString = userAgentText
        webView.reload()
    }

    fun clearCache() {
        webView.clearCache(true)
    }

    fun clearFormData() {
        webView.clearFormData()
    }

    fun currentUrl(): String = webView.url

    fun currentTitle(): String = webView.title

    fun showPageInformation() {
        PageInformationDialog(webView).show()
    }

    /**
     * Invoke site search.
     */
    fun siteSearch() {
        SiteSearch.invoke(webView)
    }

    /**
     * Reload [WebSettings].
     * @return subscription
     */
    fun reloadWebViewSettings(): Disposable {
        val settings = webView.settings
        settings.javaScriptEnabled = preferenceApplier.useJavaScript()
        settings.saveFormData = preferenceApplier.doesSaveForm()
        settings.loadsImagesAutomatically = preferenceApplier.doesLoadImage()
        return Single.create<String> { e -> e.onSuccess(preferenceApplier.userAgent()) }
                .map { uaName ->
                    val text = UserAgent.valueOf(uaName).text()

                    if (text.isNotEmpty()) {
                        text
                    } else {
                        WebView(webView.context).settings.userAgentString
                    }
                }
                .filter { settings.userAgentString != it }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { resetUserAgent(it) }
    }

    /**
     * Save archive file.
     */
    fun saveArchive() {
        if (Archive.cannotUseArchive()) {
            Toaster.snackShort(webView, R.string.message_disable_archive, colorPair)
            return
        }
        Archive.save(webView)
    }

    /**
     * Load archive file.

     * @param archiveFile
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loadArchive(archiveFile: File) {
        Archive.loadArchive(webView, archiveFile)
    }

    /**
     * Return specified index tab.
     *
     * @param index
     *
     * @return
     */
    internal fun getTabByIndex(index: Int): Tab = tabList.get(index)

    /**
     * Close specified index' tab.
     * @param index
     */
    internal fun closeTab(index: Int) {
        if (checkIndex(index)) {
            return
        }
        val tab = tabList.get(index)
        deleteThumbnail(tab.thumbnailPath)

        tabList.closeTab(index)
        setCurrentTabCount()
        if (tabList.isEmpty) {
            tabEmptyCallback()
        }
    }

    /**
     * Simple delegation.
     *
     * @param uri [Uri] object
     */
    fun loadWithNewTab(uri: Uri) {
        openNewTab(uri.toString())
    }

    /**
     * Find in page asynchronously.
     * @param text
     */
    fun find(text: String) {
        webView.findAllAsync(text)
    }

    /**
     * Find to upward.
     */
    fun findUp() {
        webView.findNext(false)
    }

    /**
     * Find to downward.
     */
    fun findDown() {
        webView.findNext(true)
    }

    fun index(): Int = tabList.getIndex()

    /**
     * Dispose this object's fields.
     */
    fun dispose() {
        webView.destroy()
        if (preferenceApplier.doesRetainTabs()) {
            tabList.save()
        } else {
            tabList.clear()
            tabsScreenshots.clean()
        }
        disposables.dispose()
        tabList.dispose()
    }

    fun loadHome() {
        loadUrl(preferenceApplier.homeUrl)
    }

    internal fun clear() {
        tabList.clear()
        setCurrentTabCount()
    }

    internal fun indexOf(tab: Tab): Int = tabList.indexOf(tab)

    fun addBookmark(callback: () -> Unit) {
        val context = webView.context
        BookmarkInsertion(
                context, webView.title, webView.url, faviconApplier.makePath(webView.url), "root").insert()
        Toaster.snackLong(
                webView,
                context.getString(R.string.message_done_added_bookmark),
                R.string.open,
                View.OnClickListener { _ -> callback()},
                colorPair
        )
    }

    internal fun currentTab(): Tab = tabList.get(index())

    private fun updateScrolled() {
        val currentTab = currentTab()
        currentTab.latest.scrolled = webView.scrollY
        tabList.set(index(), currentTab)
    }

    private fun setCurrentTabCount() {
        val size = size()
        tabCount.text = if (size < 100) "$size" else "^^"
    }

    fun moveTo(i: Int) {
        val url = currentTab().moveAndGet(i)
        if (url.isEmpty()) {
            return
        }
        loadUrl(url, false)
    }

    fun enablePullToRefresh(): Boolean = !webView.enablePullToRefresh || webView.scrollY != 0

    fun isNotEmpty(): Boolean = !tabList.isEmpty

}

