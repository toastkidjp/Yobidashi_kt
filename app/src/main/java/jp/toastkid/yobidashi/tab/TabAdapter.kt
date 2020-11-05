package jp.toastkid.yobidashi.tab

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.annotation.UiThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.archive.IdGenerator
import jp.toastkid.yobidashi.browser.archive.auto.AutoArchive
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.browser.webview.WebViewFactory
import jp.toastkid.yobidashi.browser.webview.WebViewStateUseCase
import jp.toastkid.yobidashi.browser.webview.factory.WebChromeClientFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewClientFactory
import jp.toastkid.yobidashi.libs.BitmapCompressor
import jp.toastkid.yobidashi.libs.ThumbnailGenerator
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.tab.model.ArticleListTab
import jp.toastkid.yobidashi.tab.model.ArticleTab
import jp.toastkid.yobidashi.tab.model.CalendarTab
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    private val colorPair: ColorPair

    private val tabThumbnails: TabThumbnails

    private val preferenceApplier: PreferenceApplier

    private val autoArchive = AutoArchive.make(contextSupplier())

    private val webViewStateUseCase = WebViewStateUseCase.make(contextSupplier())

    private val disposables = Job()

    private var browserHeaderViewModel: BrowserHeaderViewModel? = null

    private var appBarViewModel: AppBarViewModel? = null

    private val bitmapCompressor = BitmapCompressor()

    private var tabListViewModel: TabListViewModel? = null

    private val webViewFactory = WebViewFactory()

    init {
        val viewContext = contextSupplier()
        tabThumbnails = TabThumbnails(contextSupplier)
        preferenceApplier = PreferenceApplier(viewContext)
        colorPair = preferenceApplier.colorPair()
        if (viewContext is MainActivity) {
            val viewModelProvider = ViewModelProvider(viewContext)
            browserHeaderViewModel = viewModelProvider.get(BrowserHeaderViewModel::class.java)
            appBarViewModel = viewModelProvider.get(AppBarViewModel::class.java)
            tabListViewModel = viewModelProvider.get(TabListViewModel::class.java)
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
        val bitmap = thumbnailGenerator(view) ?: return

        val currentTab = tabList.currentTab() ?: return

        val file = tabThumbnails.assignNewFile(currentTab.thumbnailPath())
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
    fun openBackgroundTab(title: String, url: String) {
        val context = contextSupplier()
        val tabTitle =
                if (title.isNotBlank()) title
                else context.getString(R.string.new_tab)

        val newTab = WebTab.makeBackground(tabTitle, url)
        tabList.add(newTab)
        tabList.save()

        val webView = webViewFactory.make(context)
        (context as? FragmentActivity)?.also {
            webView.webViewClient = WebViewClientFactory().invoke(
                    ViewModelProvider(it).get(ContentViewModel::class.java),
                    AdRemover.make(context.assets),
                    FaviconApplier(context),
                    preferenceApplier
            )
        }

        webView.webChromeClient = WebChromeClientFactory().invoke()
        webView.loadUrl(url)
        GlobalWebViewPool.put(newTab.id(), webView)
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

    fun openNewArticleTab(title: String, onBackground: Boolean = false) {
        val articleTab = ArticleTab.make(title)
        tabList.add(articleTab)
        setCount()
        if (!onBackground) {
            setIndexByTab(articleTab)
        }
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

