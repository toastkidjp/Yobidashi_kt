package jp.toastkid.yobidashi.browser

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.ArchivesActivity
import jp.toastkid.yobidashi.browser.bookmark.BookmarkActivity
import jp.toastkid.yobidashi.browser.floating.FloatingPreview
import jp.toastkid.yobidashi.browser.history.ViewHistoryActivity
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.browser.reader.ReaderFragment
import jp.toastkid.yobidashi.browser.reader.ReaderFragmentViewModel
import jp.toastkid.yobidashi.browser.user_agent.UserAgent
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDialogFragment
import jp.toastkid.yobidashi.databinding.FragmentBrowserBinding
import jp.toastkid.yobidashi.databinding.ModuleBrowserHeaderBinding
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.editor.EditorModule
import jp.toastkid.yobidashi.editor.InputNameDialogFragment
import jp.toastkid.yobidashi.libs.ActivityOptionsFactory
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.ContentScrollable
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.menu.Menu
import jp.toastkid.yobidashi.menu.MenuViewModel
import jp.toastkid.yobidashi.pdf.PdfModule
import jp.toastkid.yobidashi.rss.extractor.RssUrlFinder
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.search.SearchQueryExtractor
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingsActivity
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.tab_list.TabListClearDialogFragment
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import jp.toastkid.yobidashi.wikipedia.today.DateArticleUrlFactory
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Internal browser fragment.
 *
 * @author toastkidjp
 */
class BrowserFragment : Fragment(),
        CommonFragmentAction,
        TabListClearDialogFragment.Callback,
        UserAgentDialogFragment.Callback,
        TabListDialogFragment.Callback,
        InputNameDialogFragment.Callback,
        ContentScrollable
{

    /**
     * RxPermissions.
     */
    private var rxPermissions: RxPermissions? = null

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * Archive folder.
     */
    private lateinit var tabs: TabAdapter

    /**
     * Tab list dialog fragment.
     */
    private var tabListDialogFragment: DialogFragment? = null

    /**
     * Search-with-clip object.
     */
    private lateinit var searchWithClip: SearchWithClip

    /**
     * Editor module.
     */
    private lateinit var editorModule: EditorModule

    /**
     * PDF module.
     */
    private lateinit var pdfModule: PdfModule

    /**
     * Browser module.
     */
    private lateinit var browserModule: BrowserModule

    /**
     * Data binding object.
     */
    private var binding: FragmentBrowserBinding? = null

    private var headerBinding: ModuleBrowserHeaderBinding? = null

    private val searchQueryExtractor = SearchQueryExtractor()

    /**
     * Composite disposer.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * Floating preview object.
     */
    private var floatingPreview: FloatingPreview? = null

    private var menuViewModel: MenuViewModel? = null

    private var headerViewModel: HeaderViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

    /**
     * Find-in-page module.
     */
    private lateinit var pageSearchPresenter: PageSearcherModule

    private lateinit var randomWikipedia: RandomWikipedia

    private val activityOptionsFactory = ActivityOptionsFactory()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            rxPermissions = RxPermissions(it)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        headerBinding = DataBindingUtil.inflate(inflater, R.layout.module_browser_header, container, false)

        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding?.fragment = this

        binding?.swipeRefresher?.let {
            it.setOnRefreshListener { tabs.reload() }
            it.setOnChildScrollUpCallback { _, _ -> browserModule.disablePullToRefresh() }
            it.setDistanceToTriggerSync(5000)
        }

        val activityContext = context ?: return null

        preferenceApplier = PreferenceApplier(activityContext)
        val colorPair = preferenceApplier.colorPair()

        val cm = activityContext.applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        searchWithClip = SearchWithClip(
                cm,
                binding?.root as View,
                colorPair
        ) { preview(it) }
        searchWithClip.invoke()

        editorModule = EditorModule(
                binding?.editor as ModuleEditorBinding,
                { intent, requestCode -> startActivityForResult(intent, requestCode) },
                { file ->
                    val currentTab = tabs.currentTab()
                    if (currentTab is EditorTab) {
                        currentTab.setFileInformation(file)
                        tabs.saveTabList()
                    }
                }
        )

        pdfModule = PdfModule(
                activityContext,
                binding?.moduleContainer as ViewGroup
        )

        browserModule = BrowserModule(
                context as Context,
                historyAddingCallback = { title, url -> tabs.addHistory(title, url) }
        )

        tabs = TabAdapter(
                binding?.webViewContainer as FrameLayout,
                browserModule,
                editorModule,
                pdfModule,
                this::onEmptyTabs
        )

        setHasOptionsMenu(true)

        headerBinding?.root?.setOnClickListener { tapHeader() }

        return binding?.root
    }

    override fun onClickInputName(fileName: String) {
        editorModule.assignNewFile(fileName)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        menuViewModel = ViewModelProviders.of(activity)
                .get(MenuViewModel::class.java)

        browserViewModel = ViewModelProviders.of(activity).get(BrowserViewModel::class.java)
        browserViewModel?.preview?.observe(activity, Observer {
            preview(it.toString())
        })
        browserViewModel?.open?.observe(activity, Observer {
            tabs.openNewWebTab(it.toString())
        })
        browserViewModel?.openBackground?.observe(activity, Observer {
            tabs.openBackgroundTab(it.toString(), it.toString())
        })
        browserViewModel?.openBackgroundWithTitle?.observe(activity, Observer {
            tabs.openBackgroundTab(it.first, it.second.toString())
        })

        pageSearchPresenter = PageSearcherModule(
                this,
                binding?.sip as ModuleSearcherBinding,
                { tabs.find(it) },
                { tabs.findDown(it) },
                { tabs.findUp(it) }
        )

        initializeHeaderViewModels(activity)
    }

    private fun initializeHeaderViewModels(activity: FragmentActivity) {
        headerViewModel = ViewModelProviders.of(activity).get(HeaderViewModel::class.java)

        headerViewModel?.stopProgress?.observe(activity, Observer { stop ->
            if (!stop || binding?.swipeRefresher?.isRefreshing == false) {
                return@Observer
            }
            stopSwipeRefresherLoading()
            tabs.saveTabList()
        })

        headerViewModel?.progress?.observe(activity, Observer { newProgress ->
            if (70 < newProgress) {
                binding?.progress?.isVisible = false
                refreshThumbnail()
                return@Observer
            }
            binding?.progress?.let {
                it.isVisible = true
                it.progress = newProgress
            }
        })

        val browserHeaderViewModel = ViewModelProviders.of(activity)
                .get(BrowserHeaderViewModel::class.java)

        browserHeaderViewModel.title.observe(activity, Observer { title ->
            if (title.isNullOrBlank()) {
                return@Observer
            }
            headerBinding?.mainText?.text = title
        })

        browserHeaderViewModel.url.observe(activity, Observer { url ->
            if (url.isNullOrBlank()) {
                return@Observer
            }
            headerBinding?.subText?.text = url
        })

        browserHeaderViewModel.reset.observe(activity, Observer {
            val headerView = headerBinding?.root ?: return@Observer
            headerViewModel?.replace(headerView)
        })
    }

    /**
     * Action on empty tabs.
     */
    private fun onEmptyTabs() {
        tabListDialogFragment?.dismiss()
        tabs.openNewWebTab()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.browser, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_tabs -> {
                switchTabList()
                return true
            }
            R.id.setting -> {
                startActivity(SettingsActivity.makeIntent(requireContext()))
                return true
            }
            R.id.add_rss -> {
                RssUrlFinder(preferenceApplier).invoke(browserModule.currentUrl()) { binding?.root }
                return true
            }
            R.id.stop_loading -> {
                stopCurrentLoading()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onMenuClick(menu: Menu) {
        val fragmentActivity = activity ?: return
        val snackbarParent = binding?.root as View
        when (menu) {
            Menu.RELOAD -> {
                tabs.reload()
            }
            Menu.BACK -> {
                tabs.back()
            }
            Menu.FORWARD-> {
                forward()
            }
            Menu.FIND_IN_PAGE-> {
                if (pageSearchPresenter.isVisible()) {
                    pageSearchPresenter.hide()
                    return
                }
                pageSearchPresenter.show(fragmentActivity)
            }
            Menu.SHARE-> {
                tabs.share()
            }
            Menu.USER_AGENT-> {
                val fragmentManager = fragmentManager ?: return
                val dialogFragment = UserAgentDialogFragment()
                dialogFragment.setTargetFragment(this, 1)
                dialogFragment.show(
                        fragmentManager,
                        UserAgentDialogFragment::class.java.simpleName
                )
            }
            Menu.READER_MODE -> {
                browserModule.invokeContentExtraction(
                        ValueCallback(this::showReaderFragment)
                )
            }
            Menu.HTML_SOURCE -> {
                browserModule.invokeHtmlSourceExtraction(
                        ValueCallback {
                            showReaderFragment(it.replace("\\u003C", "<"))
                        }
                )
            }
            Menu.PAGE_INFORMATION-> {
                val fragmentManager = fragmentManager ?: return
                PageInformationDialogFragment()
                        .also { it.arguments = tabs.makeCurrentPageInformation() }
                        .show(
                                fragmentManager,
                                PageInformationDialogFragment::class.java.simpleName
                        )
            }
            Menu.TAB_LIST-> {
                switchTabList()
            }
            Menu.STOP_LOADING-> {
                stopCurrentLoading()
            }
            Menu.ARCHIVE-> {
                browserModule.saveArchive()
            }
            Menu.VIEW_ARCHIVE -> {
                startActivityForResult(
                        ArchivesActivity.makeIntent(fragmentActivity),
                        ArchivesActivity.REQUEST_CODE
                )
            }
            Menu.RANDOM_WIKIPEDIA -> {
                if (preferenceApplier.wifiOnly &&
                        WifiConnectionChecker.isNotConnecting(requireContext())) {
                    val parent = binding?.webViewContainer ?: return
                    Toaster.snackShort(
                            parent,
                            getString(R.string.message_wifi_not_connecting),
                            colorPair()
                    )
                    return
                }

                if (!::randomWikipedia.isInitialized) {
                    randomWikipedia = RandomWikipedia()
                }
                randomWikipedia
                        .fetchWithAction { title, link ->
                            loadWithNewTab(link)
                            val parent = binding?.webViewContainer ?: return@fetchWithAction
                            Toaster.snackShort(
                                    parent,
                                    getString(R.string.message_open_random_wikipedia, title),
                                    colorPair()
                            )
                        }
                        .addTo(disposables)
            }
            Menu.WHAT_HAPPENED_TODAY -> {
                val calendar = Calendar.getInstance()
                val url = DateArticleUrlFactory()(requireContext(), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                if (Urls.isInvalidUrl(url)) {
                    return
                }
                loadWithNewTab(url.toUri())
            }
            Menu.WEB_SEARCH-> {
                search(activityOptionsFactory.makeScaleUpBundle(binding?.root as View))
            }
            Menu.VOICE_SEARCH-> {
                try {
                    startActivityForResult(VoiceSearch.makeIntent(fragmentActivity), VoiceSearch.REQUEST_CODE)
                } catch (e: ActivityNotFoundException) {
                    Timber.e(e)
                    binding?.root?.let { VoiceSearch.suggestInstallGoogleApp(it, colorPair()) }
                }
            }
            Menu.REPLACE_HOME-> {
                browserModule.currentUrl()?.let {
                    if (Urls.isInvalidUrl(it)) {
                        Toaster.snackShort(
                                snackbarParent,
                                R.string.message_cannot_replace_home_url,
                                colorPair()
                        )
                        return
                    }
                    preferenceApplier.homeUrl = it
                    Toaster.snackShort(
                            snackbarParent,
                            getString(R.string.message_replace_home_url, it) ,
                            colorPair()
                    )
                }
            }
            Menu.LOAD_HOME-> {
                tabs.loadHome()
            }
            Menu.VIEW_HISTORY-> {
                startActivityForResult(
                        ViewHistoryActivity.makeIntent(fragmentActivity),
                        ViewHistoryActivity.REQUEST_CODE
                )
            }
            Menu.BOOKMARK-> {
                context?.let {
                    startActivityForResult(
                            BookmarkActivity.makeIntent(it),
                            BookmarkActivity.REQUEST_CODE
                    )
                }
            }
            Menu.ADD_BOOKMARK-> {
                tabs.addBookmark {
                    bookmark(activityOptionsFactory.makeScaleUpBundle(binding?.root as View))
                }
            }
            Menu.EDITOR-> {
                openEditorTab()
            }
            else -> Unit
        }
    }

    private fun showReaderFragment(content: String) {
        val lineSeparator = System.getProperty("line.separator") ?: ""
        val replacedContent = content.replace("\\n", lineSeparator)

        ViewModelProviders.of(requireActivity())[ReaderFragmentViewModel::class.java]
                .close.observe(requireActivity(), Observer {
            fragmentManager?.popBackStack()
        })

        val readerFragment =
                fragmentManager?.findFragmentByTag(ReaderFragment::class.java.canonicalName)
                        ?: ReaderFragment.withContent(browserModule.currentTitle(), replacedContent)

        val transaction = fragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(
                R.anim.slide_in_right, 0, 0, android.R.anim.slide_out_right)
        transaction?.add(R.id.content, readerFragment, readerFragment::class.java.canonicalName)
        transaction?.addToBackStack(readerFragment::class.java.canonicalName)
        transaction?.commit()
    }

    /**
     * Stop current tab's loading.
     */
    private fun stopCurrentLoading() {
        browserModule.stopLoading()
        Toaster.snackShort(binding?.root as View, R.string.message_stop_loading, colorPair())
    }

    /**
     * Initialize tab list.
     */
    private fun initTabListIfNeed() {
        tabListDialogFragment = TabListDialogFragment.make(this)
    }

    /**
     * Switch tab list visibility.
     */
    private fun switchTabList() {
        initTabListIfNeed()
        if (tabListDialogFragment?.isVisible == true) {
            hideTabList()
        } else {
            tabs.updateCurrentTab()
            showTabList()
        }
    }

    /**
     * Do browser back action.
     */
    private fun back(): Boolean = tabs.back()

    /**
     * Do browser forward action.
     */
    private fun forward() = tabs.forward()

    /**
     * Show bookmark activity.
     *
     * @param option [ActivityOptions]
     */
    private fun bookmark(option: ActivityOptions) {
        val fragmentActivity = activity ?: return
        startActivityForResult(
                BookmarkActivity.makeIntent(fragmentActivity),
                BookmarkActivity.REQUEST_CODE,
                option.toBundle()
        )
    }

    /**
     * Show search activity.
     *
     * @param option [ActivityOptions]
     */
    private fun search(option: ActivityOptions) {
        context?.let {
            val currentTitle = browserModule.currentTitle()
            val currentUrl = browserModule.currentUrl()
            val query = searchQueryExtractor.invoke(currentUrl)
            val makeIntent = if (TextUtils.isEmpty(query) || Urls.isValidUrl(query)) {
                SearchActivity.makeIntent(it, currentTitle, currentUrl)
            } else {
                SearchActivity.makeIntentWithQuery(it, query ?: "", currentTitle, currentUrl)
            }
            startActivity(makeIntent, option.toBundle())
        }
    }

    /**
     * Go to bottom.
     */
    override fun toBottom() {
        tabs.pageDown()
    }

    /**
     * Go to top.
     */
    override fun toTop() {
        tabs.pageUp()
    }

    override fun onResume() {
        super.onResume()

        switchToolbarVisibility()

        val colorPair = colorPair()

        val fontColor = colorPair.fontColor()

        headerBinding?.also {
            it.icon.setColorFilter(fontColor)
            it.mainText.setTextColor(fontColor)
            it.subText.setTextColor(fontColor)
        }

        ClippingUrlOpener(binding?.root) { loadWithNewTab(it) }

        tabs.loadBackgroundTabsFromDirIfNeed()

        if (pdfModule.isVisible()) {
            pdfModule.applyColor(colorPair)
        }

        if (editorModule.isVisible()) {
            editorModule.applySettings()
        }

        if (tabs.isNotEmpty()) {
            tabs.replaceToCurrentTab(false)
        } else {
            tabs.openNewWebTab(preferenceApplier.homeUrl)
        }

        browserModule.resizePool(preferenceApplier.poolSize)
        browserModule.applyNewAlpha()

        binding?.swipeRefresher?.let {
            it.setProgressBackgroundColorSchemeColor(preferenceApplier.color)
            it.setColorSchemeColors(preferenceApplier.fontColor)
        }

        menuViewModel?.tabCount(tabs.size())
    }

    private fun switchToolbarVisibility() {
        when (preferenceApplier.browserScreenMode()) {
            ScreenMode.FULL_SCREEN -> headerViewModel?.hide()
            ScreenMode.EXPANDABLE, ScreenMode.FIXED -> headerViewModel?.show()
        }
    }

    override fun pressLongBack(): Boolean {
        activity?.let {
            startActivityForResult(
                ViewHistoryActivity.makeIntent(it),
                ViewHistoryActivity.REQUEST_CODE
            )
        }
        return true
    }

    override fun pressBack(): Boolean = hideOption() || back()

    private fun tapHeader() {
        val activityContext = context ?: return
        val currentTitle = browserModule.currentTitle()
        val currentUrl = browserModule.currentUrl()
        val inputText = if (preferenceApplier.enableSearchQueryExtract) {
            searchQueryExtractor(currentUrl) ?: currentUrl
        } else {
            currentUrl
        }

        startActivity(
                SearchActivity.makeIntentWithQuery(
                        activityContext,
                        inputText ?: "",
                        currentTitle,
                        currentUrl
                )
        )
    }

    /**
     * Hide option menus.
     */
    private fun hideOption(): Boolean {

        if (tabListDialogFragment?.isVisible == true) {
            hideTabList()
            return true
        }

        if (pageSearchPresenter.isVisible()) {
            pageSearchPresenter.hide()
            return true
        }

        return false
    }

    /**
     * Hide tab list.
     */
    private fun hideTabList() {
        tabListDialogFragment?.dismiss()
        tabs.replaceToCurrentTab(false)
        menuViewModel?.tabCount(tabs.size())
    }

    /**
     * Show tab list.
     */
    private fun showTabList() {
        refreshThumbnail()
        val fragmentManager = fragmentManager ?: return
        tabListDialogFragment?.show(fragmentManager, "")
    }

    private fun refreshThumbnail() {
        val currentTab = tabs.currentTab()
        tabs.deleteThumbnail(currentTab?.thumbnailPath)
        tabs.saveNewThumbnailAsync()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return
        }
        when (requestCode) {
            ArchivesActivity.REQUEST_CODE -> {
                loadArchive(ArchivesActivity.extractFile(intent))
            }
            VoiceSearch.REQUEST_CODE -> {
                activity?.let {
                    VoiceSearch.processResult(it, intent).addTo(disposables)
                }
            }
            BookmarkActivity.REQUEST_CODE, ViewHistoryActivity.REQUEST_CODE -> {
                intent.data?.let {
                    tabs.openNewWebTab(it.toString())
                }
            }
            EditorModule.REQUEST_CODE_LOAD -> {
                intent.data?.let { editorModule.readFromFileUri(it) }
            }
            EditorModule.REQUEST_CODE_LOAD_AS -> {
                intent.data?.let {
                    editorModule.readFromFileUri(it)
                    editorModule.saveAs()
                }
            }
        }
    }

    /**
     * Open Editor tab.
     */
    private fun openEditorTab() {
        rxPermissions
                ?.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ?.subscribe(
                        { granted ->
                            if (!granted) {
                                val context = activity ?: return@subscribe
                                Toaster.tShort(context, R.string.message_requires_permission_storage)
                                return@subscribe
                            }
                            tabs.openNewEditorTab()
                            tabs.replaceToCurrentTab()
                        },
                        Timber::e
                )
                ?.addTo(disposables)
    }

    /**
     * Load archive file.
     *
     * @param file Archive file
     */
    fun loadArchive(file: File) {
        try {
            tabs.loadArchive(file)
        } catch (e: IOException) {
            Timber.e(e)
        } catch (error: Throwable) {
            Timber.e(error)
            System.gc()
        }
    }

    /**
     * Load with opening new tab.
     *
     * @param uri [Uri]
     */
    fun loadWithNewTab(uri: Uri) {
        tabs.openNewWebTab(uri.toString())
    }

    private fun preview(url: String) {
        val webView = browserModule.getWebView(FloatingPreview.getSpecialId())
        if (webView == null) {
            Toaster.snackLong(
                    binding?.root as View,
                    R.string.message_preview_failed,
                    R.string.retry,
                    View.OnClickListener { preview(url) },
                    colorPair()
                    )
            return
        }

        if (floatingPreview == null) {
            floatingPreview = FloatingPreview(requireContext())
        }

        binding?.root?.let {
            floatingPreview?.show(it, webView, url)
        }
    }
    
    private fun colorPair() = preferenceApplier.colorPair()

    override fun onClickClear() {
        tabs.clear()
        onEmptyTabs()
    }

    override fun onClickUserAgent(userAgent: UserAgent) {
        browserModule.resetUserAgent(userAgent.text())
        Toaster.snackShort(
                binding?.root as View,
                getString(R.string.format_result_user_agent, userAgent.title()),
                preferenceApplier.colorPair()
        )
    }

    override fun onCloseTabListDialogFragment() = hideTabList()

    override fun onOpenEditor() = openEditorTab()

    //TODO override fun onOpenPdf() = openPdfTabFromStorage()

    override fun openNewTabFromTabList() = tabs.openNewWebTab()

    override fun tabIndexFromTabList() = tabs.index()

    override fun currentTabIdFromTabList() = tabs.currentTabId()

    override fun replaceTabFromTabList(tab: Tab) {
        tabs.replace(tab)
        stopSwipeRefresherLoading()
    }

    override fun getTabByIndexFromTabList(position: Int): Tab? = tabs.getTabByIndex(position)

    override fun closeTabFromTabList(position: Int) {
        tabs.closeTab(position)
        stopSwipeRefresherLoading()
    }

    override fun getTabAdapterSizeFromTabList(): Int = tabs.size()

    override fun swapTabsFromTabList(from: Int, to: Int) = tabs.swap(from, to)

    override fun tabIndexOfFromTabList(tab: Tab): Int = tabs.indexOf(tab)

    private fun stopSwipeRefresherLoading() {
        binding?.swipeRefresher?.isRefreshing = false
    }

    override fun onPause() {
        super.onPause()
        stopSwipeRefresherLoading()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        browserModule.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        browserModule.onViewStateRestored(savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        tabs.saveTabList()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabs.dispose()
        disposables.clear()
        searchWithClip.dispose()
        headerViewModel?.show()
        browserModule.dispose()
        pageSearchPresenter.dispose()
        pdfModule.dispose()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val layoutId = R.layout.fragment_browser

    }

}
