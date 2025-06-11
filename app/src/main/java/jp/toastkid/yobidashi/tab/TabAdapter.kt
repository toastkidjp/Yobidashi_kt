package jp.toastkid.yobidashi.tab

import android.content.Context
import android.net.Uri
import android.os.Message
import android.view.View
import androidx.activity.ComponentActivity
import androidx.annotation.UiThread
import androidx.core.net.toFile
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.image.BitmapCompressor
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.thumbnail.ThumbnailGenerator
import jp.toastkid.web.archive.IdGenerator
import jp.toastkid.web.archive.auto.AutoArchive
import jp.toastkid.web.webview.GlobalWebViewPool
import jp.toastkid.web.webview.WebViewStateUseCase
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.tab.model.ArticleListTab
import jp.toastkid.yobidashi.tab.model.ArticleTab
import jp.toastkid.yobidashi.tab.model.CalendarTab
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    private val tabThumbnails: TabThumbnails

    private val preferenceApplier: PreferenceApplier

    private val autoArchive = AutoArchive.make(contextSupplier())

    private val webViewStateUseCase = WebViewStateUseCase.make(contextSupplier())

    private val bitmapCompressor = BitmapCompressor()

    private var contentViewModel: ContentViewModel? = null

    init {
        val viewContext = contextSupplier()
        tabThumbnails = TabThumbnails.with(contextSupplier())
        preferenceApplier = PreferenceApplier(viewContext)

        if (viewContext is ComponentActivity) {
            val viewModelProvider = ViewModelProvider(viewContext)
            contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
        }

        CoroutineScope(Dispatchers.IO).launch {
            tabThumbnails.deleteUnused(tabList.thumbnailNames())
            autoArchive.deleteUnused(tabList.archiveIds())
            webViewStateUseCase.deleteUnused(tabList.ids())
        }
    }

    private val thumbnailGenerator = ThumbnailGenerator()

    /**
     * Save new thumbnail asynchronously.
     */
    @UiThread
    fun saveNewThumbnail(view: View?) {
        val currentTab = tabList.currentTab() ?: return

        val file = tabThumbnails.assignNewFile(currentTab.thumbnailPath())
        if (System.currentTimeMillis() - file.lastModified() < 15_000) {
            return
        }

        val bitmap = thumbnailGenerator(view) ?: return
        bitmapCompressor(bitmap, file)
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
    fun openBackgroundTab(title: String, url: String): Tab {
        val context = contextSupplier()
        val tabTitle =
            title.ifBlank { context.getString(R.string.new_tab) }

        val newTab = WebTab.make(tabTitle, url)
        tabList.add(newTab)
        tabList.save()

        setCount()
        return newTab
    }

    /**
     * Open background tab with URL string.
     *
     * @param message [Message]
     */
    fun openNewWindowWebTab(message: Message): Tab {
        val context = contextSupplier()
        val title = message.data?.getString("title")
        val url = message.data?.getString("url") ?: ""

        val tabTitle =
            if (title.isNullOrBlank()) context.getString(R.string.new_tab)
            else title

        val newTab = WebTab.make(tabTitle, url)
        tabList.add(newTab)
        tabList.save()

        setIndexByTab(newTab)
        setCount()

        return newTab
    }

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
        val lastOrNull = uri.pathSegments.lastOrNull()
        val title = if (lastOrNull == null) "PDF Viewer" else "[PDF] " + lastOrNull
        pdfTab.setTitle(title)
        pdfTab.setPath(uri.toString())
        tabList.add(pdfTab)
        setCount()
        setIndexByTab(pdfTab)
    }

    fun openNewArticleTab(title: String, onBackground: Boolean = false): Tab {
        val articleTab = ArticleTab.make(title)
        tabList.add(articleTab)
        setCount()
        if (!onBackground) {
            setIndexByTab(articleTab)
        }

        return articleTab
    }

    fun openArticleList() {
        val newTab = ArticleListTab.withTitle(contextSupplier().getString(R.string.title_article_viewer))
        tabList.add(newTab)
        setCount()
        setIndexByTab(newTab)
    }

    fun openCalendar() {
        val newTab = CalendarTab.withTitle(contextSupplier().getString(R.string.title_calendar))
        tabList.add(newTab)
        setCount()
        setIndexByTab(newTab)
    }

    /**
     *
     * @param tab
     */
    fun setIndexByTab(tab: Tab) {
        val newIndex = tabList.indexOf(tab)
        if (invalidIndex(newIndex)) {
            return
        }

        tabList.setIndex(newIndex)
    }

    internal fun replace(tab: Tab) {
        setIndexByTab(tab)
    }

    fun movePreviousTab() {
        val current = index()
        if (current == 0) {
            return
        }

        tabList.setIndex(current - 1)
    }

    fun moveNextTab() {
        val current = index()
        if (tabList.size() <= current) {
            return
        }

        tabList.setIndex(current + 1)
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
            webViewStateUseCase.delete(tab.id())
            GlobalWebViewPool.remove(tab.id())
        }

        tabList.closeTab(index)
        tabThumbnails.delete(tab?.thumbnailPath())
        setCount()
        if (tabList.isEmpty) {
            tabEmptyCallback()
        }
    }

    fun closeCurrentTab() {
        closeTab(index())
    }

    fun index(): Int = tabList.getIndex()

    fun saveTabList() {
        tabList.save()
    }

    /**
     * Dispose this object's fields.
     */
    private fun dispose() {
        if (!preferenceApplier.doesRetainTabs()) {
            tabList.clear()
            tabThumbnails.clean()
        }
    }

    internal fun clear() {
        tabList.clear()
        tabThumbnails.clean()
    }

    internal fun indexOf(tab: Tab): Int = tabList.indexOf(tab)

    internal fun currentTab(): Tab? = tabList.get(index())

    fun currentTabIsWebTab() = currentTab() is WebTab

    internal fun currentTabId(): String = currentTab()?.id() ?: "-1"

    fun isEmpty(): Boolean = tabList.isEmpty

    override fun toString(): String = tabList.toString()

    fun swap(from: Int, to: Int) = tabList.swap(from, to)

    fun updateWebTab(idAndHistory: Pair<String, History>) {
        tabList.updateWithIdAndHistory(idAndHistory)
    }

    private fun setCount() {
        contentViewModel?.tabCount(size())
    }

    fun onLifecycleEvent(event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> setCount()
            Lifecycle.Event.ON_PAUSE -> saveTabList()
            Lifecycle.Event.ON_DESTROY -> dispose()
            else -> Unit
        }
    }

}

