package jp.toastkid.yobidashi.tab

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.archive.IdGenerator
import jp.toastkid.yobidashi.browser.archive.auto.AutoArchive
import jp.toastkid.yobidashi.libs.BitmapCompressor
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import jp.toastkid.yobidashi.tab.tab_list.TabListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    private val tabThumbnails: TabThumbnails

    private val preferenceApplier: PreferenceApplier

    private val autoArchive = AutoArchive.make(contextSupplier())

    private val disposables = Job()

    private var browserHeaderViewModel: BrowserHeaderViewModel? = null

    private var headerViewModel: HeaderViewModel? = null

    private val bitmapCompressor = BitmapCompressor()

    private var tabListViewModel: TabListViewModel? = null

    init {
        val viewContext = contextSupplier()
        tabThumbnails = TabThumbnails(contextSupplier)
        preferenceApplier = PreferenceApplier(viewContext)
        colorPair = preferenceApplier.colorPair()
        if (viewContext is MainActivity) {
            val viewModelProvider = ViewModelProvider(viewContext)
            browserHeaderViewModel = viewModelProvider.get(BrowserHeaderViewModel::class.java)
            headerViewModel = viewModelProvider.get(HeaderViewModel::class.java)
            tabListViewModel = viewModelProvider.get(TabListViewModel::class.java)
        }
    }
    /**
     * Save new thumbnail asynchronously.
     */
    fun saveNewThumbnailAsync(makeDrawingCache: () -> Bitmap?) {
        val currentTab = tabList.currentTab() ?: return
        makeDrawingCache()?.let {
            CoroutineScope(Dispatchers.Default).launch(disposables) {
                val file = tabThumbnails.assignNewFile(currentTab.thumbnailPath())
                bitmapCompressor(it, file)
            }
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
     *
     * @param url URL (default = homeUrl)
     */
    fun openNewWebTab(url: String = preferenceApplier.homeUrl) {
        val newTab = WebTab.make("New tab: $url", url)
        tabList.add(newTab)
        setCount()
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
        setCount()
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
        setCount()
        setIndexByTab(editorTab)
    }

    fun openNewPdfTab(uri: Uri) {
        val pdfTab = PdfTab()
        pdfTab.setPath(uri.toString())
        tabList.add(pdfTab)
        setCount()
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
            autoArchive.delete(IdGenerator().from(tab.getUrl()))
        }

        tabList.closeTab(index)
        tabThumbnails.delete(tab?.thumbnailPath())
        setCount()
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
            tabThumbnails.clean()
        }
        disposables.cancel()
    }

    internal fun clear() {
        tabList.clear()
        tabThumbnails.clean()
    }

    internal fun indexOf(tab: Tab): Int = tabList.indexOf(tab)

    internal fun currentTab(): Tab? = tabList.get(index())

    internal fun currentTabId(): String = currentTab()?.id() ?: "-1"

    fun isEmpty(): Boolean = tabList.isEmpty

    fun isNotEmpty(): Boolean = !tabList.isEmpty

    override fun toString(): String = tabList.toString()

    fun swap(from: Int, to: Int) = tabList.swap(from, to)

    fun updateWebTab(idAndHistory: Pair<String, History>?) {
        val id = idAndHistory?.first
        if (currentTabId() != id) {
            return
        }

        tabList.updateWithIdAndHistory(idAndHistory)
    }

    fun setCount() {
        tabListViewModel?.tabCount(size())
    }

}

