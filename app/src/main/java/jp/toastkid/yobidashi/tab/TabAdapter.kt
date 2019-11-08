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
import androidx.lifecycle.ViewModelProviders
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.*
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
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.pdf.PdfModule
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

    private var browserHeaderViewModel: BrowserHeaderViewModel? = null

    private var headerViewModel: HeaderViewModel? = null

    init {
        val viewContext = webViewContainer.context
        tabsScreenshots = makeNewScreenshotDir(viewContext)
        preferenceApplier = PreferenceApplier(viewContext)
        colorPair = preferenceApplier.colorPair()
        if (viewContext is MainActivity) {
            browserHeaderViewModel =
                    ViewModelProviders.of(viewContext).get(BrowserHeaderViewModel::class.java)
            headerViewModel =
                    ViewModelProviders.of(viewContext).get(HeaderViewModel::class.java)
        }
    }
    /**
     * Save new thumbnail asynchronously.
     */
    fun saveNewThumbnailAsync() {
        val currentTab = tabList.currentTab() ?: return
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
                is PdfTab -> {
                    pdf.makeThumbnail()
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
    fun openNewWebTab(url: String = preferenceApplier.homeUrl, withLoad: Boolean = true) {
        val newTab = WebTab()
        if (Urls.isValidUrl(url)) {
            newTab.histories.add(0, History("New tab: $url", url))
        }
        tabList.add(newTab)
        setIndexByTab(newTab)
        replaceWebView()
        if (withLoad) {
            callLoadUrl(url)
        }
        Toaster.snackShort(
                webViewContainer,
                webViewContainer.context.getString(R.string.message_tab_open_new, url),
                preferenceApplier.colorPair()
        )
    }

    /**
     * Open background tab with URL string.
     *
     * @param url
     */
    fun openBackgroundTab(url: String) {
        tabList.add(WebTab.makeBackground(webViewContainer.context.getString(R.string.new_tab), url))
        tabList.save()
        Toaster.snackShort(
                webViewContainer,
                webViewContainer.context.getString(R.string.message_tab_open_background, url),
                preferenceApplier.colorPair()
        )
    }

    /**
     * This method allow calling from only [BrowserFragment].
     */
    internal fun openNewEditorTab() {
        val editorTab = EditorTab()
        tabList.add(editorTab)
        setIndexByTab(editorTab)
    }

    /**
     * Open new PDF tab with [Uri].
     *
     * @param uri
     */
    internal fun openNewPdfTab(uri: Uri) {
        val pdfTab = PdfTab().apply {
            setTitle(uri.path ?: "")
            setPath(uri.toString())
        }
        tabList.add(pdfTab)
        setIndexByTab(pdfTab)
    }

    fun back() = browserModule.back()

    fun forward() = browserModule.forward()

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
            val previousView = webViewContainer[0]
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
            it.isEnabled = true
            it.visibility = View.VISIBLE
            it.onResume()
            webViewContainer.addView(it)

            val mainActivity = webViewContainer.context
            if (mainActivity is MainActivity
                    && preferenceApplier.browserScreenMode() != ScreenMode.FULL_SCREEN) {
                mainActivity.showToolbar()
            }
            browserModule.animate(slideUpFromBottom)
        }

        browserModule.reloadWebViewSettings().addTo(disposables)

        currentTab()?.let {
            if (TextUtils.isEmpty(browserModule.currentUrl()) && Urls.isValidUrl(it.getUrl())) {
                callLoadUrl(it.getUrl())
                return@let
            }
            browserHeaderViewModel?.nextTitle(it.title())
            browserHeaderViewModel?.nextUrl(it.getUrl())
        }
    }

    internal fun replace(tab: Tab) {
        setIndexByTab(tab)
        //TODO replaceToCurrentTab(true)
    }

    /**
     * Replace visibilities for current tab.
     *
     * @param withAnimation for suppress redundant animation.
     */
    fun replaceToCurrentTab(withAnimation: Boolean = true) {
        when (val currentTab = tabList.currentTab()) {
            is WebTab -> {
                replaceWebView()
                if (editor.isVisible()) {
                    editor.hide()
                }
                if (pdf.isVisible()) {
                    pdf.hide()
                }
                browserHeaderViewModel?.resetContent()
            }
            is EditorTab -> {
                if (currentTab.path.isNotBlank()) {
                    editor.readFromFile(File(currentTab.path))
                } else {
                    editor.clearPath()
                }

                if (pdf.isVisible()) {
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
                if (editor.isVisible()) {
                    editor.hide()
                }
                pdf.show()

                val url: String = currentTab.getUrl()
                if (url.isNotEmpty()) {
                    try {
                        val uri = Uri.parse(url)
                        pdf.load(uri)
                        pdf.scrollTo(currentTab.getScrolled())
                        saveNewThumbnailAsync()

                        browserHeaderViewModel?.nextTitle(PDF_TAB_TITLE)
                        browserHeaderViewModel?.nextUrl(uri.lastPathSegment ?: url)
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

    fun callLoadUrl(url: String) {
        browserModule.loadUrl(url)
        if (editor.isVisible()) {
            editor.hide()
        }
    }

    private fun failRead(e: Throwable) {
        Timber.e(e)
        Toaster.snackShort(webViewContainer, R.string.message_failed_tab_read, colorPair)
        closeTab(index())
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
            is EditorTab -> editor.pageUp()
        }
    }

    fun pageDown() {
        when (currentTab()) {
            is WebTab -> browserModule.pageDown()
            is PdfTab -> pdf.pageDown()
            is EditorTab -> editor.pageDown()
        }
    }

    /**
     * Load archive file.

     * @param archiveFile
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun loadArchive(archiveFile: File) {
        openNewWebTab(withLoad = true)
        currentWebView()?.let { Archive.loadArchive(it, archiveFile) }
    }

    /**
     * Return specified index tab.
     *
     * @param index
     *
     * @return
     */
    internal fun getTabByIndex(index: Int): Tab? = tabList.get(index)

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
            browserModule.disableWebView()
            browserModule.detachWebView(tab.id())
        }

        tabList.closeTab(index)
        if (tabList.isEmpty) {
            tabEmptyCallback()
        }
    }

    /**
     * Find in page asynchronously.
     * @param text
     */
    fun find(text: String) {
        when (currentTab()) {
            is WebTab -> browserModule.findAllAsync(text)
            is PdfTab -> Unit
            is EditorTab -> Unit
        }
    }

    /**
     * Find to upward.
     */
    fun findUp(text: String) {
        when (currentTab()) {
            is WebTab -> browserModule.findUp()
            is PdfTab -> Unit
            is EditorTab -> editor.findUp(text)
        }
    }

    /**
     * Find to downward.
     */
    fun findDown(text: String) {
        when (currentTab()) {
            is WebTab -> browserModule.findDown()
            is PdfTab -> Unit
            is EditorTab -> editor.findDown(text)
        }
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
                View.OnClickListener { callback() },
                colorPair
        )
    }

    internal fun currentTab(): Tab? = tabList.get(index())

    internal fun currentTabId(): String = currentTab()?.id() ?: "-1"

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

    fun swap(from: Int, to: Int) = tabList.swap(from, to)

    /**
     * It's simple delegation.
     */
    fun loadBackgroundTabsFromDirIfNeed() {
        tabList.loadBackgroundTabsFromDirIfNeed()
    }

    fun makeCurrentPageInformation(): Bundle = browserModule.makeCurrentPageInformation()

    private fun currentWebView() = browserModule.getWebView(currentTabId())

    companion object {

        /**
         * Directory path to screenshot.
         */
        private const val SCREENSHOT_DIR_PATH: String = "tabs/screenshots"

        /**
         * PDF tab's dummy title.
         */
        private const val PDF_TAB_TITLE: String = "PDF Tab"

        /**
         * Make new screenshot dir wrapper instance.
         */
        fun makeNewScreenshotDir(context: Context): FilesDir = FilesDir(context, SCREENSHOT_DIR_PATH)

    }

}

