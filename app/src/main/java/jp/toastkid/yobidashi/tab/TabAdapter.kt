package jp.toastkid.yobidashi.tab

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.ViewModelProviders
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.archive.auto.AutoArchive
import jp.toastkid.yobidashi.libs.BitmapCompressor
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import timber.log.Timber
import java.io.File

/**
 * ModuleAdapter of [Tab].
 *
 * @author toastkidjp
 */
class TabAdapter(
        private val contextSupplier: () -> Context,
        private val tabEmptyCallback: () -> Unit
) {

    private val tabList: TabList = TabList.loadOrInit(contextSupplier())

    private val colorPair: ColorPair

    private val tabsScreenshots: FilesDir

    private val preferenceApplier: PreferenceApplier

    private val autoArchive = AutoArchive.make(contextSupplier())

    private val disposables: CompositeDisposable = CompositeDisposable()

    private var browserHeaderViewModel: BrowserHeaderViewModel? = null

    private var headerViewModel: HeaderViewModel? = null

    private val bitmapCompressor = BitmapCompressor()

    init {
        val viewContext = contextSupplier()
        tabsScreenshots = makeNewScreenshotDir(viewContext)
        preferenceApplier = PreferenceApplier(viewContext)
        colorPair = preferenceApplier.colorPair()
        if (viewContext is MainActivity) {
            val viewModelProvider = ViewModelProviders.of(viewContext)
            browserHeaderViewModel = viewModelProvider.get(BrowserHeaderViewModel::class.java)
            headerViewModel = viewModelProvider.get(HeaderViewModel::class.java)
        }
    }
    /**
     * Save new thumbnail asynchronously.
     */
    fun saveNewThumbnailAsync(makeDrawingCache: () -> Bitmap?) {
        val currentTab = tabList.currentTab() ?: return
        makeDrawingCache()?.let {
            Completable.fromAction {
                val file = tabsScreenshots.assignNewFile("${currentTab.id()}.png")
                bitmapCompressor(it, file)
                currentTab.thumbnailPath = file.absolutePath
            }.subscribeOn(Schedulers.io())
                    .subscribe({}, Timber::e)
                    .addTo(disposables)
        }
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
     * TODO remove withLoad.
     */
    fun openNewWebTab(url: String = preferenceApplier.homeUrl, withLoad: Boolean = true) {
        val newTab = WebTab()
        if (Urls.isValidUrl(url)) {
            newTab.histories.add(0, History("New tab: $url", url))
        }
        tabList.add(newTab)
        setIndexByTab(newTab)
    }

    /**
     * Open background tab with URL string.
     *
     * @param title Tab's title
     * @param url Tab's URL
     */
    fun openBackgroundTab(title: String, url: String) {
        val tabTitle =
                if (title.isNotBlank()) title
                else contextSupplier().getString(R.string.new_tab)

        tabList.add(WebTab.makeBackground(tabTitle, url))
        tabList.save()
    }

    /**
     * This method allow calling from only [BrowserFragment].
     */
    internal fun openNewEditorTab(path: String? = null) {
        val editorTab = EditorTab()
        if (path != null) {
            editorTab.path = path
        }
        tabList.add(editorTab)
        setIndexByTab(editorTab)
    }

    fun openNewPdfTab(uri: Uri) {
        val pdfTab = PdfTab()
        pdfTab.setPath(uri.toString())
        tabList.add(pdfTab)
        setIndexByTab(pdfTab)
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

    internal fun replace(tab: Tab) {
        setIndexByTab(tab)
        //TODO replaceToCurrentTab(true)
    }

    private fun invalidIndex(newIndex: Int): Boolean = newIndex < 0 || tabList.size() <= newIndex

    /**
     * Return current tab count.
     *
     * @return tab count
     */
    fun size(): Int = tabList.size()

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
            autoArchive.delete(tab.id())
            /* TODO
            if (index == this.index()) {
                browserModule.animate(slideDown)
                browserModule.disableWebView()
                browserModule.detachWebView(tab.id())
            }
             */
        }

        tabList.closeTab(index)
        if (tabList.isEmpty) {
            tabEmptyCallback()
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
        disposables.clear()
        tabList.dispose()
    }

    internal fun clear() {
        tabList.clear()
    }

    internal fun indexOf(tab: Tab): Int = tabList.indexOf(tab)

    fun addBookmark(callback: () -> Unit) {
        /* TODO check it
        val context = contextSupplier()
        val url = browserModule.currentUrl() ?: ""
        BookmarkInsertion(
                context,
                browserModule.currentTitle(),
                url,
                faviconApplier.makePath(url),
                Bookmark.getRootFolderName()
        ).insert()

        Toaster.snackLong(
                webViewContainer,
                context.getString(R.string.message_done_added_bookmark),
                R.string.open,
                View.OnClickListener { callback() },
                colorPair
        )
         */
    }

    internal fun currentTab(): Tab? = tabList.get(index())

    internal fun currentTabId(): String = currentTab()?.id() ?: "-1"

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

