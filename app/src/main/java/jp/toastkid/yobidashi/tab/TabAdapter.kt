package jp.toastkid.yobidashi.tab

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.get
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.BrowserModule
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.TitlePair
import jp.toastkid.yobidashi.browser.archive.Archive
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.Bookmarks
import jp.toastkid.yobidashi.editor.EditorModule
import jp.toastkid.yobidashi.libs.Bitmaps
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.pdf.PdfModule
import jp.toastkid.yobidashi.search.SiteSearch
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * ModuleAdapter of [Tab].
 *
 * @author toastkidjp
 */
class TabAdapter(
        private val webViewContainer: FrameLayout,
        private val browserModule: BrowserModule,
        private val editor: EditorModule,
        private val pdf: PdfModule,
        private val titleCallback: (TitlePair) -> Unit,
        private val tabEmptyCallback: () -> Unit
) {

    private val tabList: TabList = TabList.loadOrInit(webViewContainer.context)

    private val colorPair: ColorPair

    private val tabsScreenshots: FilesDir

    private val faviconApplier: FaviconApplier = FaviconApplier(webViewContainer.context)

    private val preferenceApplier: PreferenceApplier

    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * Animation of slide up bottom.
     */
    private val slideUpFromBottom
            = AnimationUtils.loadAnimation(webViewContainer.context, R.anim.slide_up)

    init {
        val viewContext = webViewContainer.context
        tabsScreenshots = makeNewScreenshotDir(viewContext)
        preferenceApplier = PreferenceApplier(viewContext)
        colorPair = preferenceApplier.colorPair()
        setCurrentTabCount()
    }
    /**
     * Save new thumbnail asynchronously.
     */
    fun saveNewThumbnailAsync() {
        val currentTab = tabList.currentTab()
        makeDrawingCache(currentTab)?.let {
            Completable.fromAction {
                val file = tabsScreenshots.assignNewFile("${currentTab.id()}.png")
                Bitmaps.compress(it, file)
                currentTab.thumbnailPath = file.absolutePath
            }.subscribeOn(Schedulers.io())
                    .subscribe({}, Timber::e)
                    .addTo(disposables)
        }
    }

    /**
     * Make drawing cache.
     *
     * @param tab
     */
    private fun makeDrawingCache(tab: Tab): Bitmap? =
            when (tab) {
                is WebTab -> {
                    browserModule.makeDrawingCache()
                }
                is EditorTab -> {
                    editor.makeThumbnail()
                }
                else -> null
            }

    /**
     * Delete thumbnail file.
     *
     * @param thumbnailPath file path.
     */
    fun deleteThumbnail(thumbnailPath: String?) {
        if (TextUtils.isEmpty(thumbnailPath)) {
            return
        }

        val lastScreenshot = File(thumbnailPath)
        if (lastScreenshot.exists()) {
            lastScreenshot.delete()
        }
    }

    /**
     * Open new tab with URL string.
     */
    fun openNewWebTab(url: String = "") {
        val newTab = WebTab()
        if (Urls.isValidUrl(url)) {
            newTab.histories.add(0, History("", url))
        }
        tabList.add(newTab)
        setIndexByTab(newTab)
        replaceWebView()
        callLoadUrl(url)
    }

    /**
     * Open background tab with URL string.
     *
     * @param url
     */
    fun openBackgroundTab(url: String) {
        tabList.add(WebTab.makeBackground(webViewContainer.context.getString(R.string.new_tab), url))
        tabList.save()
    }

    /**
     * This method allow calling from only [BrowserFragment].
     */
    internal fun openNewEditorTab() {
        val editorTab = EditorTab()
        tabList.add(editorTab)
        setCurrentTabCount()
        setIndexByTab(editorTab)
    }

    /**
     * Open new PDF tab with [Uri].
     *
     * @param uri
     */
    internal fun openNewPdfTab(uri: Uri) {
        val pdfTab = PdfTab().apply {
            setTitle(uri.path)
            setPath(uri.toString())
        }
        tabList.add(pdfTab)
        setCurrentTabCount()
        setIndexByTab(pdfTab)
    }

    fun back(): Boolean {
        return currentWebView()?.let {
            if (it.canGoBack()) {
                it.goBack()
                return true
            }
            return false
        } ?: false
    }

    fun forward() = currentWebView()?.let {
        if (it.canGoForward()) {
            it.goForward()
        }
    }

    /**
     *
     * @param tab
     */
    private fun setIndexByTab(tab: Tab) {
        val newIndex = tabList.indexOf(tab)
        if (invalidIndex(newIndex)) {
            return
        }

        tabList.setIndex(newIndex)
    }

    private fun replaceWebView() {
        val currentWebView = currentWebView()
        if (webViewContainer.childCount != 0) {
            val previousView = webViewContainer.get(0)
            if (currentWebView != previousView) {
                if (previousView is WebView) {
                    previousView.stopLoading()
                    previousView.onPause()
                }
                webViewContainer.removeView(previousView)
            }
        }

        currentWebView?.let {
            if (it.parent != null) {
                return@let
            }
            webViewContainer.addView(it, 0, MATCH_PARENT)
            it.onResume()
            browserModule.animate(slideUpFromBottom)
        }

        browserModule.reloadWebViewSettings().addTo(disposables)
        browserModule.enableWebView()

        currentTab()?.getUrl()?.let {
            if (TextUtils.isEmpty(currentWebView?.url) && Urls.isValidUrl(it)) {
                callLoadUrl(it)
            }
        }
    }

    internal fun replace(tab: Tab) {
        setIndexByTab(tab)
        replaceToCurrentTab(true)
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
                replaceWebView()
                if (editor.isVisible) {
                    editor.hide()
                }
                if (pdf.isVisible) {
                    pdf.hide()
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

                browserModule.disableWebView()
                saveNewThumbnailAsync()
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
                        pdf.scrollTo(currentTab.getScrolled())
                        pdf.assignNewThumbnail(currentTab).addTo(disposables)
                        titleCallback(TitlePair.make(PDF_TAB_TITLE, uri.lastPathSegment ?: url))
                    } catch (e: SecurityException) {
                        failRead(e)
                        return
                    } catch (e: IllegalStateException) {
                        failRead(e)
                        return
                    }
                }

                if (withAnimation) {
                    pdf.animate(slideUpFromBottom)
                }

                browserModule.disableWebView()
                tabList.save()
            }
        }
    }

    private fun callLoadUrl(url: String, saveHistory: Boolean = true) {
        browserModule.loadUrl(url, saveHistory)
        if (editor.isVisible) {
            editor.hide()
        }
    }

    private fun failRead(e: Throwable) {
        Timber.e(e)
        Toaster.snackShort(webViewContainer, R.string.message_failed_tab_read, colorPair)
        closeTab(index())
        return
    }

    private fun invalidIndex(newIndex: Int): Boolean = newIndex < 0 || tabList.size() <= newIndex

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
        browserModule.reload()
    }

    fun pageUp() {
        when (currentTab()) {
            is WebTab -> browserModule.pageUp()
            is PdfTab -> pdf.pageUp()
        }
    }

    fun pageDown() {
        when (currentTab()) {
            is WebTab -> browserModule.pageDown()
            is PdfTab -> pdf.pageDown()
        }
    }

    /**
     * Invoke site search.
     */
    fun siteSearch() {
        if (currentTab() is WebTab) {
            currentWebView()?.let { SiteSearch.invoke(it) }
            return
        }
        Toaster.snackShort(webViewContainer, "This menu can be used on only web page.", colorPair)
    }

    /**
     * Load archive file.

     * @param archiveFile
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loadArchive(archiveFile: File) {
        currentWebView()?.let { Archive.loadArchive(it, archiveFile) }
    }

    private fun currentWebView() = browserModule.currentView(currentTabId())

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
        if (invalidIndex(index)) {
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
     * Find in page asynchronously.
     * @param text
     */
    fun find(text: String) {
        browserModule.findAllAsync(text)
    }

    /**
     * Find to upward.
     */
    fun findUp() {
        browserModule.findUp()
    }

    /**
     * Find to downward.
     */
    fun findDown() {
        browserModule.findDown()
    }

    fun index(): Int = tabList.getIndex()

    fun saveTabList() {
        tabList.save()
    }

    /**
     * Dispose this object's fields.
     */
    fun dispose() {
        if (!preferenceApplier.doesRetainTabs()) {
            tabList.clear()
            tabsScreenshots.clean()
        }
        browserModule.dispose()
        disposables.clear()
        tabList.dispose()
    }

    fun loadHome() {
        callLoadUrl(preferenceApplier.homeUrl)
    }

    internal fun clear() {
        tabList.clear()
        setCurrentTabCount()
    }

    internal fun indexOf(tab: Tab): Int = tabList.indexOf(tab)

    /**
     * Add history.
     *
     * @param title
     * @param url
     */
    fun addHistory(title: String, url: String) {
        val currentTab = tabList.currentTab()
        if (currentTab is WebTab) {
            currentTab.addHistory(History.make(title, url))
        }
    }

    fun addBookmark(callback: () -> Unit) {
        val context = webViewContainer.context
        val url = browserModule.currentUrl() ?: ""
        BookmarkInsertion(
                context,
                browserModule.currentTitle(),
                url,
                faviconApplier.makePath(url),
                Bookmarks.ROOT_FOLDER_NAME
        ).insert()

        Toaster.snackLong(
                webViewContainer,
                context.getString(R.string.message_done_added_bookmark),
                R.string.open,
                View.OnClickListener { _ -> callback()},
                colorPair
        )
    }

    internal fun currentTab(): Tab? = tabList.get(index())

    internal fun currentTabId(): String = currentTab()?.id() ?: "-1"

    /**
     * TODO remove.
     */
    private fun setCurrentTabCount() = Unit

    fun moveTo(i: Int) {
        val currentTab = currentTab()
        if (currentTab is WebTab) {
            val url = currentTab.moveAndGet(i)
            if (url.isEmpty()) {
                return
            }
            callLoadUrl(url, false)
        }
    }

    /**
     * Update current tab state.
     */
    fun updateCurrentTab() {
        val currentTab = currentTab()
        if (currentTab is PdfTab) {
            currentTab.setScrolled(pdf.currentItemPosition())
            tabList.set(index(), currentTab)
        }
    }

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

    fun makeCurrentPageInformation(): Bundle = browserModule.makeCurrentPageInformation()

    companion object {

        /**
         * Directory path to screenshot.
         */
        private const val SCREENSHOT_DIR_PATH: String = "tabs/screenshots"

        /**
         * PDF tab's dummy title.
         */
        private const val PDF_TAB_TITLE: String = "PDF Tab"

        private val MATCH_PARENT = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        )

        /**
         * Make new screenshot dir wrapper instance.
         */
        fun makeNewScreenshotDir(context: Context): FilesDir = FilesDir(context, SCREENSHOT_DIR_PATH)

    }

}

