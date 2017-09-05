package jp.toastkid.yobidashi.browser.tab

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import jp.toastkid.yobidashi.R
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
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.Storeroom
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SiteSearch
import timber.log.Timber
import java.io.File
import java.io.IOException


/**
 * ModuleAdapter of [Tab].
 *
 * @author toastkidjp
 */
class TabAdapter(
        progress: ProgressBar,
        webViewContainer: FrameLayout,
        titleCallback: (TitlePair) -> Unit,
        touchCallback: () -> Unit,
        private val tabEmptyCallback: () -> Unit
) {

    private val tabList: TabList

    private val colorPair: ColorPair

    private val webView: WebView

    /** Loading flag.  */
    private var isLoadFinished: Boolean = false

    private var backOrForwardProgress: Boolean = false

    private val favicons: Storeroom

    private val tabsScreenshots: Storeroom

    private val preferenceApplier: PreferenceApplier

    init {
        tabList = TabList.loadOrInit(progress.context)

        webView = makeWebView(progress, titleCallback, touchCallback)
        webViewContainer.addView(this.webView)

        favicons = Storeroom(webView.context, "favicons")

        tabsScreenshots = Storeroom(webView.context, "tabs/screenshots")
        preferenceApplier = PreferenceApplier(webView.context)
        colorPair = preferenceApplier.colorPair()
    }

    private fun makeWebView(
            progress: ProgressBar,
            titleCallback: (TitlePair) -> Unit,
            touchCallback: () -> Unit
    ): WebView {
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
                                        makeFaviconPath(urlstr)
                                )
                                .insert()
                    }
                }
                backOrForwardProgress = false
            }

            override fun onReceivedError(
                    view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                backOrForwardProgress = false
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
                    val file = favicons.assignNewFile(Uri.parse(view.url).host + ".png")
                    Bitmaps.compress(favicon, file)
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
                    ImageDownloadAction(webView, hitResult).invoke()
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
                                    0 -> {
                                        openNewTab(url)
                                        setIndex(tabList.size() - 1)
                                    }
                                    1 -> openNewTab(url)
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            WebIconDatabase.getInstance()
                    .open(webView.context.getDir("favicons", Context.MODE_PRIVATE).path);
        }
        return webView
    }

    private fun makeFaviconPath(urlstr: String): String {
        return favicons.assignNewFile(Uri.parse(urlstr).host + ".png").absolutePath
    }

    private fun saveNewThumbnail() {
        webView.invalidate()
        webView.buildDrawingCache()
        val file = tabsScreenshots.assignNewFile(System.currentTimeMillis().toString() + ".png")
        Bitmaps.compress(webView.drawingCache, file)
        tabList.currentTab().thumbnailPath = file.absolutePath
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

    internal fun openNewTab(url: String) {
        val newTab = Tab()
        newTab.addHistory(History.make(url, url))
        tabList.add(newTab)
        tabList.save()
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

    fun setIndex(newIndex: Int) {

        if (checkIndex(newIndex)) {
            return
        }
        tabList.setIndex(newIndex)
        backOrForwardProgress = true

        val latest = tabList.currentTab().latest
        if (latest !== History.EMPTY) {
            loadUrl(latest.url())
        }
    }

    internal fun setIndexByTab(tab: Tab) {
        setIndex(tabList.indexOf(tab))
    }

    private fun checkIndex(newIndex: Int): Boolean {
        return newIndex < 0 || tabList.size() <= newIndex
    }

    fun size(): Int {
        return tabList.size()
    }

    fun reload() {
        webView.reload()
    }

    fun reloadUrlIfNeed() {
        loadUrl(tabList.currentTab().latest.url())
    }

    fun loadUrl(url: String) {
        if (TextUtils.equals(webView.url, url)) {
            return
        }
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

    fun currentUrl(): String {
        return webView.url
    }

    fun currentTitle(): String {
        return webView.title
    }

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
    internal fun getTabByIndex(index: Int): Tab {
        return tabList.get(index)
    }

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
        if (tabList.isEmpty) {
            tabEmptyCallback()
        }
    }

    fun loadWithNewTab(uri: Uri) {
        openNewTab()
        setIndex(tabList.size() - 1)
        loadUrl(uri.toString())
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

    fun index(): Int {
        return tabList.getIndex()
    }

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
    }

    fun loadHome() {
        loadUrl(preferenceApplier.homeUrl)
    }

    internal fun clear(adapter: Adapter?) {
        (0..tabList.size() - 1).forEach {
            closeTab(it)
            adapter?.notifyItemRemoved(it)
        }
        tabList.save()
    }

    internal fun indexOf(tab: Tab): Int {
        return tabList.indexOf(tab)
    }

    fun addBookmark(callback: () -> Unit) {
        val context = webView.context
        BookmarkInsertion(
                context, webView.title, webView.url, makeFaviconPath(webView.url), "root").insert()
        Toaster.snackLong(
                webView,
                context.getString(R.string.message_done_added_bookmark),
                R.string.open,
                View.OnClickListener { _ -> callback()},
                colorPair
        )
    }

}

