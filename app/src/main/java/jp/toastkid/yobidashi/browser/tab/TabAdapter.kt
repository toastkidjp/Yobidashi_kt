package jp.toastkid.yobidashi.browser.tab

import android.annotation.TargetApi
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.*
import android.widget.TextView
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.*
import jp.toastkid.yobidashi.browser.archive.Archive
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.Bookmarks
import jp.toastkid.yobidashi.browser.history.ViewHistoryInsertion
import jp.toastkid.yobidashi.browser.pdf.PdfModule
import jp.toastkid.yobidashi.browser.screenshots.Screenshot
import jp.toastkid.yobidashi.browser.tab.model.EditorTab
import jp.toastkid.yobidashi.browser.tab.model.PdfTab
import jp.toastkid.yobidashi.browser.tab.model.Tab
import jp.toastkid.yobidashi.browser.tab.model.WebTab
import jp.toastkid.yobidashi.browser.webview.CustomWebView
import jp.toastkid.yobidashi.browser.webview.WebViewFactory
import jp.toastkid.yobidashi.editor.EditorModule
import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SiteSearch
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity
import okhttp3.Request
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
        private val webViewContainer: ViewGroup,
        private val editor: EditorModule,
        private val pdf: PdfModule,
        private val tabCount: TextView,
        private val titleCallback: (TitlePair) -> Unit,
        private val loadingCallback: (Int, Boolean) -> Unit,
        touchCallback: () -> Boolean,
        private val scrollCallback: (Boolean) -> Unit,
        private val tabEmptyCallback: () -> Unit
) {

    private val tabList: TabList = TabList.loadOrInit(webViewContainer.context)

    private val colorPair: ColorPair

    private val webView: CustomWebView

    /** Loading flag.  */
    private var isLoadFinished: Boolean = false

    private var backOrForwardProgress: Boolean = false

    private val tabsScreenshots: FilesDir

    private val faviconApplier: FaviconApplier = FaviconApplier(webViewContainer.context)

    private val preferenceApplier: PreferenceApplier

    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * Animation of slide up bottom.
     */
    private val slideUpFromBottom
            = AnimationUtils.loadAnimation(tabCount.context, R.anim.slide_up)

    init {
        webView = makeWebView(titleCallback, touchCallback)
        webViewContainer.addView(this.webView)

        tabsScreenshots = makeNewScreenshotDir(webView.context)
        preferenceApplier = PreferenceApplier(webView.context)
        colorPair = preferenceApplier.colorPair()
        setCurrentTabCount()
    }

    private fun makeWebView(
            titleCallback: (TitlePair) -> Unit,
            touchCallback: () -> Boolean
    ): CustomWebView {
        val webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingCallback(0, true)
                isLoadFinished = false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                isLoadFinished = true
                loadingCallback(100, false)

                val currentTab = currentTab()
                val lastScrolled = currentTab.getScrolled()
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

                if (currentTab is WebTab) {
                    deleteThumbnail(currentTab.thumbnailPath)
                }

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
                tabList.save()
            }

            override fun onReceivedError(
                    view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                backOrForwardProgress = false
                loadingCallback(100, false)
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

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
                    shouldOverrideUrlLoading(view, request?.url?.toString())

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    val context: Context? = view?.context
                    val uri: Uri = Uri.parse(url)
                    if (it.startsWith("tel:")) {
                        context?.startActivity(IntentFactory.dial(uri))
                        view?.reload()
                        return true
                    }
                    if (it.startsWith("mailto:")) {
                        context?.startActivity(IntentFactory.mailTo(uri))
                        view?.reload()
                        return true
                    }
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }

        val webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

                loadingCallback(newProgress, newProgress < 65)

                if (!isLoadFinished) {
                    try {
                        titleCallback(
                                TitlePair.make(view.context.getString(R.string.prefix_loading) + newProgress + "%", view.url ?: "")
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

        val webView = WebViewFactory.make(tabCount.context)
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
                    AlertDialog.Builder(webView.context)
                            .setTitle("Image: " + url)
                            .setItems(R.array.image_menu, { dialog, which ->
                                when (which) {
                                    0 -> {
                                        storeImage(url, webView).subscribe{file ->
                                            preferenceApplier.backgroundImagePath = file.absolutePath
                                            Toaster.snackShort(
                                                    webView,
                                                    R.string.message_change_background_image,
                                                    preferenceApplier.colorPair()
                                            )
                                        }.addTo(disposables)
                                    }
                                    1 -> storeImage(url, webView).subscribe({
                                        Toaster.snackShort(
                                                webView,
                                                R.string.message_done_save,
                                                preferenceApplier.colorPair()
                                        )
                                    }).addTo(disposables)
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
                    AlertDialog.Builder(tabCount.context)
                            .setTitle("URL: " + url)
                            .setItems(R.array.url_menu, { _, which ->
                                when (which) {
                                    0 -> openNewTab(url)
                                    1 -> openBackgroundTab(url)
                                    2 -> loadUrl(url)
                                    3 -> Clipboard.clip(v.context, url)
                                }
                            })
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
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)
            request.allowScanningByMediaScanner()
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.lastPathSegment)
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
            if (Math.abs(scrolled) > MINIMUM_SCROLLED && currentTab() is WebTab) {
                scrollCallback(0 > scrolled)
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            WebIconDatabase.getInstance()
                    .open(webView.context.getDir("faviconApplier", Context.MODE_PRIVATE).path)
        }
        return webView
    }

    /**
     * TODO replace with fromCallable.
     *
     * @param url
     * @param webView
     */
    private fun storeImage(url: String, webView: WebView): Maybe<File> {
        return Single.fromCallable {
            HTTP_CLIENT.newCall(Request.Builder().url(url).build()).execute()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .filter { it.code() == HttpURLConnection.HTTP_OK }
                .map { BitmapFactory.decodeStream(it.body()?.byteStream()) }
                .map {
                    val storeroom = FilesDir(webView.context, BackgroundSettingActivity.BACKGROUND_DIR)
                    val file = storeroom.assignNewFile(Uri.parse(url))
                    Bitmaps.compress(it, file)
                    file
                }
    }

    private fun saveNewThumbnail() {
        webView.invalidate()
        webView.buildDrawingCache()

        val currentTab = tabList.currentTab()
        if (currentTab is WebTab) {
            val file = tabsScreenshots.assignNewFile("${currentTab.id()}.png")
            val drawingCache = webView.drawingCache
            if (drawingCache != null) {
                Bitmaps.compress(drawingCache, file)
            }
            currentTab.thumbnailPath = file.absolutePath
        }
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

    internal fun openNewEditorTab() {
        val editorTab = EditorTab()
        tabList.add(editorTab)
        setCurrentTabCount()
        setIndexByTab(editorTab, true)
    }

    internal fun openNewPdfTab(uri: Uri) {
        val pdfTab = PdfTab().apply {
            setTitle(uri.path)
            setPath(uri.toString())
        }
        tabList.add(pdfTab)
        setCurrentTabCount()
        setIndexByTab(pdfTab, true)
    }

    internal fun openNewTab() {
        openNewTab(preferenceApplier.homeUrl)
    }

    private fun openNewTab(url: String) {
        val newTab = WebTab()
        tabList.add(newTab)
        setCurrentTabCount()
        setIndexByTab(newTab, true)
        loadUrl(url)
    }

    private fun openBackgroundTab(url: String) {
        tabList.add(WebTab.makeBackground(webView.context.getString(R.string.new_tab), url))
        tabList.save()
        setCurrentTabCount()
    }

    private fun addHistory(title: String, url: String) {
        val currentTab = tabList.currentTab()
        if (currentTab is WebTab) {
            currentTab.addHistory(History.make(title, url))
        }
    }

    fun back(): String {
        backOrForwardProgress = true
        return tabList.currentTab().back()
    }

    fun forward(): String {
        backOrForwardProgress = true
        return tabList.currentTab().forward()
    }

    internal fun setCurrentTab() {
        if (size() <= 0) {
            return
        }
        setIndexByTab(currentTab())
    }

    /**
     *
     * @param tab
     * @param openNew default false
     */
    internal fun setIndexByTab(tab: Tab, openNew: Boolean = false) {
        val index = tabList.indexOf(tab)
        updateScrolled()

        if (openNew) {
            setIndex(index)
            webView.startAnimation(slideUpFromBottom)
            return
        }

        setIndex(index)
    }

    private fun setIndex(newIndex: Int) {

        if (checkIndex(newIndex)) {
            return
        }
        tabList.setIndex(newIndex)
    }

    /**
     * Replace visibilities for current tab.
     *
     * @param withAnimation for suppress redundant animation.
     */
    fun replaceToCurrentTab(withAnimation: Boolean = true) {
        val currentTab = tabList.currentTab()
        when (currentTab) {
            is WebTab -> {
                if (editor.isVisible) {
                    editor.hide()
                    enableWebView()
                }
                if (pdf.isVisible) {
                    pdf.hide()
                    enableWebView()
                }
                val latest = currentTab.latest
                if (latest !== History.EMPTY) {
                    loadUrl(latest.url())
                }
            }
            is EditorTab -> {
                if (currentTab.path.isNotBlank()) {
                    editor.readFromFile(File(currentTab.path))
                } else {
                    editor.clearPath()
                }

                if (pdf.isVisible) {
                    pdf.hide()
                }
                editor.show()
                if (withAnimation) {
                    editor.animate(slideUpFromBottom)
                }

                disableWebView()
                tabList.save()
            }
            is PdfTab -> {
                if (editor.isVisible) {
                    editor.hide()
                }
                pdf.show()

                val url: String = currentTab.getUrl()
                if (url.isNotEmpty()) {
                    try {
                        val uri = Uri.parse(url)
                        pdf.load(uri)
                        currentTab.thumbnailPath = pdf.assignNewThumbnail(currentTab.id())
                        titleCallback(TitlePair.make(PDF_TAB_TITLE, uri.lastPathSegment ?: url))
                    } catch (e: SecurityException) {
                        Timber.e(e)
                    }
                }

                if (withAnimation) {
                    pdf.animate(slideUpFromBottom)
                }

                disableWebView()
                tabList.save()
            }
        }
    }

    /**
     * Enable [WebView].
     */
    private inline fun enableWebView() {
        webView.isEnabled = true
    }

    /**
     * Disble [WebView].
     */
    private inline fun disableWebView() {
        webView.isEnabled = false
        stopLoading()
    }

    private fun checkIndex(newIndex: Int): Boolean = newIndex < 0 || tabList.size() <= newIndex

    /**
     * Return current tab count.
     *
     * @return tab count
     */
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
        replaceToCurrentTab()
    }

    fun loadUrl(url: String, saveHistory: Boolean = true) {
        if (url.isEmpty()) {
            return
        }
        if (TextUtils.equals(webView.url, url)) {
            webView.scrollTo(0, currentTab().getScrolled())
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

    fun currentUrl(): String? = webView.url

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
        if (tab is WebTab) {
            deleteThumbnail(tab.thumbnailPath)
        }

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

    fun saveTabList() {
        tabList.save()
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
        webView.destroy()
        disposables.clear()
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
                context, webView.title, webView.url, faviconApplier.makePath(webView.url), Bookmarks.ROOT_FOLDER_NAME).insert()
        Toaster.snackLong(
                webView,
                context.getString(R.string.message_done_added_bookmark),
                R.string.open,
                View.OnClickListener { _ -> callback()},
                colorPair
        )
    }

    internal fun currentTab(): Tab = tabList.get(index())

    internal fun currentTabId(): String = currentTab().id()

    private fun updateScrolled() {
        val currentTab = currentTab()
        currentTab.setScrolled(webView.scrollY)
        tabList.set(index(), currentTab)
    }

    private fun setCurrentTabCount() {
        val size = size()
        tabCount.text = if (size < 100) "$size" else "^^"
    }

    fun moveTo(i: Int) {
        val currentTab = currentTab()
        if (currentTab is WebTab) {
            val url = currentTab.moveAndGet(i)
            if (url.isEmpty()) {
                return
            }
            loadUrl(url, false)
        }
    }

    /**
     * Stop loading in current tab.
     */
    fun stopLoading() {
        webView.stopLoading()
    }

    /**
     * Is disable Pull-to-Refresh?
     */
    fun disablePullToRefresh(): Boolean = !webView.enablePullToRefresh || webView.scrollY != 0

    fun isEmpty(): Boolean = tabList.isEmpty

    fun isNotEmpty(): Boolean = !tabList.isEmpty

    override fun toString(): String = tabList.toString()

    /**
     * It's simple delegation.
     */
    fun loadBackgroundTabsFromDirIfNeed() {
        tabList.loadBackgroundTabsFromDirIfNeed()
        setCurrentTabCount()
    }

    companion object {

        /**
         * Directory path to screenshot.
         */
        private const val SCREENSHOT_DIR_PATH: String = "tabs/screenshots";

        /**
         * Suppressing unnecessary animation.
         */
        private const val MINIMUM_SCROLLED: Int = 10

        /**
         * PDF tab's dummy title.
         */
        private const val PDF_TAB_TITLE: String = "PDF Tab"

        /**
         * HTTP Client.
         */
        private val HTTP_CLIENT by lazy { HttpClientFactory.make() }

        /**
         * Make new screenshot dir wrapper instance.
         */
        fun makeNewScreenshotDir(context: Context): FilesDir = FilesDir(context, SCREENSHOT_DIR_PATH)

    }
}

