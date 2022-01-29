package jp.toastkid.yobidashi.main

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.FileExtractorFromUri
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.input.Inputs
import jp.toastkid.lib.intent.OpenDocumentIntentFactory
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.tab.TabUiFragment
import jp.toastkid.lib.view.ToolbarColorApplier
import jp.toastkid.lib.view.WindowOptionColorApplier
import jp.toastkid.lib.view.filter.color.ForegroundColorFilterUseCase
import jp.toastkid.media.music.popup.permission.ReadAudioPermissionRequestContract
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.BrowserFragmentViewModel
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.bookmark.BookmarkFragment
import jp.toastkid.yobidashi.browser.floating.FloatingPreview
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.browser.permission.DownloadPermissionRequestContract
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.databinding.ActivityMainBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.image.BackgroundImageLoaderUseCase
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.main.launch.ElseCaseUseCase
import jp.toastkid.yobidashi.main.launch.LauncherIntentUseCase
import jp.toastkid.yobidashi.main.launch.RandomWikipediaUseCase
import jp.toastkid.yobidashi.main.usecase.ArticleTabOpenerUseCase
import jp.toastkid.yobidashi.main.usecase.BackgroundTabOpenerUseCase
import jp.toastkid.yobidashi.main.usecase.MusicPlayerUseCase
import jp.toastkid.yobidashi.menu.MenuBinder
import jp.toastkid.yobidashi.menu.MenuSwitchColorApplier
import jp.toastkid.yobidashi.menu.MenuUseCase
import jp.toastkid.yobidashi.menu.MenuViewModel
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchFragment
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.usecase.SearchFragmentFactoryUseCase
import jp.toastkid.yobidashi.settings.SettingFragment
import jp.toastkid.yobidashi.settings.fragment.OverlayColorFilterViewModel
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Main activity of this app.
 *
 * @author toastkidjp
 */
class MainActivity : AppCompatActivity(), TabListDialogFragment.Callback {

    /**
     * Data binding object.
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var tabs: TabAdapter

    private var floatingPreview: FloatingPreview? = null

    /**
     * Menu's view model.
     */
    private var menuViewModel: MenuViewModel? = null

    private var contentViewModel: ContentViewModel? = null

    private var tabListViewModel: TabListViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

    /**
     * Search-with-clip object.
     */
    private lateinit var searchWithClip: SearchWithClip

    private val backgroundImageLoaderUseCase by lazy { BackgroundImageLoaderUseCase() }

    /**
     * Find-in-page module.
     */
    private lateinit var pageSearchPresenter: PageSearcherModule

    private lateinit var tabReplacingUseCase: TabReplacingUseCase

    private lateinit var onBackPressedUseCase: OnBackPressedUseCase

    private lateinit var appBarVisibilityUseCase: AppBarVisibilityUseCase

    private lateinit var fragmentReplacingUseCase: FragmentReplacingUseCase

    private var tabListUseCase: TabListUseCase? = null

    private lateinit var menuSwitchColorApplier: MenuSwitchColorApplier

    private var musicPlayerUseCase: MusicPlayerUseCase? = null

    private var activityResultLauncher: ActivityResultLauncher<Intent>? =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            val data = it.data ?: return@registerForActivityResult
            val uri = data.data ?: return@registerForActivityResult
            val takeFlags: Int =
                data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver?.takePersistableUriPermission(uri, takeFlags)

            tabs.openNewPdfTab(uri)
            replaceToCurrentTab(true)
            tabListUseCase?.dismiss()
        }

    private val requestPermissionForOpenPdfTab =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                return@registerForActivityResult
            }

            activityResultLauncher?.launch(OpenDocumentIntentFactory()("application/pdf"))
        }

    private val mediaPermissionRequestLauncher =
        registerForActivityResult(ReadAudioPermissionRequestContract()) {
            it.second?.invoke(it.first)
        }

    private val musicPlayerBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            musicPlayerUseCase?.invoke(binding.root)
        }
    }

    private val downloadPermissionRequestLauncher =
        registerForActivityResult(DownloadPermissionRequestContract()) {
            if (it.first.not()) {
                contentViewModel?.snackShort(R.string.message_requires_permission_storage)
                return@registerForActivityResult
            }
            val url = it.second ?: return@registerForActivityResult
            DownloadAction(this).invoke(url)
        }

    /**
     * Disposables.
     */
    private val disposables: Job by lazy { Job() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)

        setSupportActionBar(binding.toolbar)

        fragmentReplacingUseCase = FragmentReplacingUseCase(supportFragmentManager)

        val colorPair = preferenceApplier.colorPair()

        pageSearchPresenter = PageSearcherModule(binding.sip)

        appBarVisibilityUseCase = AppBarVisibilityUseCase(binding.toolbar, preferenceApplier)

        initializeHeaderViewModel()

        initializeContentViewModel()

        initializeMenuViewModel()

        menuSwitchColorApplier = MenuSwitchColorApplier(binding.menuSwitch)

        val activityViewModelProvider = ViewModelProvider(this)
        browserViewModel = activityViewModelProvider.get(BrowserViewModel::class.java)
        browserViewModel?.preview?.observe(this, Observer {
            val uri = it?.getContentIfNotHandled() ?: return@Observer
            Inputs.hideKeyboard(binding.content)

            if (floatingPreview == null) {
                floatingPreview = FloatingPreview(this)
            }
            floatingPreview?.show(binding.root, uri.toString())
        })
        browserViewModel?.open?.observe(this, Observer {
            val uri = it?.getContentIfNotHandled() ?: return@Observer
            openNewWebTab(uri)
        })

        val backgroundTabOpenerUseCase = BackgroundTabOpenerUseCase(
                binding.content,
                { title, url -> tabs.openBackgroundTab(title, url) },
                { replaceToCurrentTab(true) }
        )

        browserViewModel?.openBackground?.observe(this, Observer {
            val urlString = it?.getContentIfNotHandled()?.toString() ?: return@Observer
            backgroundTabOpenerUseCase(urlString, urlString, preferenceApplier.colorPair())
        })
        browserViewModel?.openBackgroundWithTitle?.observe(this, Observer {
            val pair = it?.getContentIfNotHandled() ?: return@Observer
            backgroundTabOpenerUseCase(pair.first, pair.second.toString(), preferenceApplier.colorPair())
        })
        browserViewModel?.download?.observe(this, Observer {
            val url = it?.getContentIfNotHandled() ?: return@Observer
            downloadPermissionRequestLauncher.launch(url)
        })

        invokeSearchWithClip(colorPair)

        CoroutineScope(Dispatchers.Main).launch {
            activityViewModelProvider.get(LoadingViewModel::class.java)
                    .onPageFinished
                    .collect {
                        if (it.expired()) {
                            return@collect
                        }

                        tabs.updateWebTab(it.tabId to it.history)
                        if (tabs.currentTabId() == it.tabId) {
                            refreshThumbnail()
                        }
                    }
        }

        activityViewModelProvider.get(OverlayColorFilterViewModel::class.java)
                .newColor
                .observe(this, {
                    updateColorFilter()
                })

        tabListViewModel = activityViewModelProvider.get(TabListViewModel::class.java)
        tabListViewModel
                ?.saveEditorTab
                ?.observe(
                        this,
                        Observer {
                            val currentTab = tabs.currentTab() as? EditorTab ?: return@Observer
                            currentTab.setFileInformation(it)
                            tabs.saveTabList()
                        }
                )
        tabListViewModel
            ?.openNewTab
            ?.observe(this, { openNewTabFromTabList() })

        supportFragmentManager.setFragmentResultListener("clear_tabs", this, { key, result ->
            if (result.getBoolean(key).not()) {
                return@setFragmentResultListener
            }
            onClickClear()
        })

        tabs = TabAdapter({ this }, this::onEmptyTabs)

        tabReplacingUseCase = TabReplacingUseCase(
                tabs,
                ::obtainFragment,
                { fragment, animation -> replaceFragment(fragment, animation) },
                activityViewModelProvider.get(BrowserFragmentViewModel::class.java),
                ::refreshThumbnail,
                {
                    CoroutineScope(Dispatchers.IO).launch(disposables) {
                        runOnUiThread(it)
                    }
                }
        )

        processShortcut(intent)

        onBackPressedUseCase = OnBackPressedUseCase(
                tabListUseCase,
                { binding.menuStub.root?.isVisible == true },
                menuViewModel,
                pageSearchPresenter,
                { floatingPreview },
                tabs,
                ::onEmptyTabs,
                tabReplacingUseCase,
                supportFragmentManager
        )

        registerReceiver(musicPlayerBroadcastReceiver, IntentFilter("jp.toastkid.music.action.open"))

        supportFragmentManager.addOnBackStackChangedListener {
            val findFragment = findFragment()

            if (findFragment !is TabUiFragment && supportFragmentManager.backStackEntryCount == 0) {
                finish()
            }
        }
    }

    private fun invokeSearchWithClip(colorPair: ColorPair) {
        searchWithClip = SearchWithClip(
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
            binding.content,
            colorPair,
            browserViewModel,
            preferenceApplier
        )
        searchWithClip.invoke()
    }

    private fun obtainFragment(fragmentClass: Class<out Fragment>) =
            supportFragmentManager.findFragmentByTag(fragmentClass.canonicalName)
                    ?: fragmentClass.newInstance()

    private fun initializeHeaderViewModel() {
        val headerViewModel = ViewModelProvider(this).get(AppBarViewModel::class.java)
        headerViewModel.content.observe(this, Observer { view ->
            if (view == null) {
                return@Observer
            }
            binding.toolbarContent.removeAllViews()

            if (view.parent != null) {
                (view.parent as? ViewGroup)?.removeAllViews()
            }

            if (view.layoutParams != null) {
                binding.toolbar.layoutParams.height = view.layoutParams.height
            }
            binding.toolbarContent.addView(view, 0)
        })

        headerViewModel.visibility.observe(this, { isVisible ->
            if (isVisible) appBarVisibilityUseCase.show() else appBarVisibilityUseCase.hide()
        })
    }

    private fun initializeMenuViewModel() {
        menuViewModel = ViewModelProvider(this).get(MenuViewModel::class.java)

        MenuBinder(this, menuViewModel, binding.menuStub, binding.menuSwitch)

        musicPlayerUseCase = MusicPlayerUseCase(mediaPermissionRequestLauncher)
        MenuUseCase({ this }, menuViewModel, contentViewModel, musicPlayerUseCase).observe()
    }

    private fun initializeContentViewModel() {
        contentViewModel = ViewModelProvider(this).get(ContentViewModel::class.java)
        contentViewModel?.fragmentClass?.observe(this, Observer {
            val fragmentClass = it?.getContentIfNotHandled() ?: return@Observer
            replaceFragment(obtainFragment(fragmentClass), withAnimation = true, withSlideIn = true)
        })
        contentViewModel?.fragment?.observe(this, Observer {
            val fragment = it?.getContentIfNotHandled() ?: return@Observer
            replaceFragment(fragment, withAnimation = true, withSlideIn = false)
        })
        contentViewModel?.snackbar?.observe(this, Observer {
            val snackbarEvent = it.getContentIfNotHandled() ?: return@Observer
            if (snackbarEvent.actionLabel == null) {
                Toaster.snackShort(
                        binding.content,
                        snackbarEvent.message,
                        preferenceApplier.colorPair()
                )
                return@Observer
            }

            Toaster.withAction(
                binding.content,
                snackbarEvent.message,
                snackbarEvent.actionLabel ?: "",
                { snackbarEvent.action() },
                preferenceApplier.colorPair()
            )
        })
        contentViewModel?.snackbarRes?.observe(this, Observer {
            val messageId = it?.getContentIfNotHandled() ?: return@Observer
            Toaster.snackShort(binding.content, messageId, preferenceApplier.colorPair())
        })
        contentViewModel?.toTop?.observe(this, {
            (findFragment() as? ContentScrollable)?.toTop()
        })
        contentViewModel?.toBottom?.observe(this, {
            (findFragment() as? ContentScrollable)?.toBottom()
        })
        contentViewModel?.share?.observe(this, Observer {
            if (it.hasBeenHandled) {
                return@Observer
            }
            it.getContentIfNotHandled()
            (findFragment() as? CommonFragmentAction)?.share()
        })
        contentViewModel?.webSearch?.observe(this, {
            when (val fragment = findFragment()) {
                is BrowserFragment -> {
                    val titleAndUrl = fragment.getTitleAndUrl()
                    replaceFragment(SearchFragmentFactoryUseCase().invoke(titleAndUrl))
                }
                else ->
                    contentViewModel?.nextFragment(SearchFragment::class.java)
            }
        })
        contentViewModel?.openPdf?.observe(this, {
            openPdfTabFromStorage()
        })
        contentViewModel?.openEditorTab?.observe(this, {
            openEditorTab()
        })
        contentViewModel?.switchPageSearcher?.observe(this, {
            pageSearchPresenter.switch()
        })
        contentViewModel?.switchTabList?.observe(this, Observer {
            it?.getContentIfNotHandled() ?: return@Observer
            switchTabList()
        })
        contentViewModel?.refresh?.observe(this, {
            refresh()
        })
        contentViewModel?.newArticle?.observe(this, Observer {
            val titleAndOnBackground = it?.getContentIfNotHandled() ?: return@Observer
            ArticleTabOpenerUseCase(
                tabs,
                binding.content
            ) { replaceToCurrentTab() }.invoke(titleAndOnBackground.first, titleAndOnBackground.second, preferenceApplier.colorPair())
        })
        contentViewModel?.openArticleList?.observe(this, {
            tabs.openArticleList()
            replaceToCurrentTab()
        })
        contentViewModel?.openCalendar?.observe(this, {
            tabs.openCalendar()
            replaceToCurrentTab()
        })
    }

    override fun onNewIntent(passedIntent: Intent) {
        super.onNewIntent(passedIntent)
        processShortcut(passedIntent)
    }

    /**
     * Process intent shortcut.
     *
     * @param calledIntent
     */
    private fun processShortcut(calledIntent: Intent) {
        LauncherIntentUseCase(
                RandomWikipediaUseCase(
                        contentViewModel,
                        this::openNewWebTab,
                        { id, param -> getString(id, param) }
                ),
                this::openNewWebTab,
                { openEditorTab(FileExtractorFromUri(this, it)) },
                ::search,
                {
                    preferenceApplier.getDefaultSearchEngine()
                            ?: SearchCategory.getDefaultCategoryName()
                },
                { replaceFragment(obtainFragment(it)) },
                ElseCaseUseCase(
                        tabs::isEmpty,
                        ::openNewTab,
                        { supportFragmentManager.findFragmentById(R.id.content) },
                        { replaceToCurrentTab(false) }
                )
        ).invoke(calledIntent)
    }

    private fun search(category: String?, query: String?) {
        if (category.isNullOrEmpty() || query.isNullOrEmpty()) {
            return
        }

        SearchAction(this, category, query).invoke()
    }

    private fun openNewWebTab(uri: Uri) {
        tabs.openNewWebTab(uri.toString())
        replaceToCurrentTab(true)
    }

    /**
     * Replace with passed fragment.
     *
     * @param fragment {@link BaseFragment} instance
     */
    private fun replaceFragment(
            fragment: Fragment,
            withAnimation: Boolean = true,
            withSlideIn: Boolean = false
    ) {
        fragmentReplacingUseCase.invoke(fragment, withAnimation, withSlideIn)
    }

    /**
     * Replace visibilities for current tab.
     *
     * @param withAnimation for suppress redundant animation.
     */
    private fun replaceToCurrentTab(withAnimation: Boolean = true) {
        tabReplacingUseCase.invoke(withAnimation)
    }

    private fun refreshThumbnail() {
        CoroutineScope(Dispatchers.Default).launch(disposables) {
            runOnUiThread {
                val findFragment = findFragment()
                if (findFragment !is TabUiFragment) {
                    return@runOnUiThread
                }
                tabs.saveNewThumbnail(binding.content)
            }
        }
    }

    override fun onBackPressed() {
        onBackPressedUseCase.invoke()
    }

    private fun findFragment() = supportFragmentManager.findFragmentById(R.id.content)

    override fun onResume() {
        super.onResume()
        refresh()
        menuViewModel?.onResume()
        floatingPreview?.onResume()

        tabs.setCount()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        ClippingUrlOpener()(binding.content) { browserViewModel?.open(it) }
    }

    /**
     * Refresh toolbar and background.
     */
    private fun refresh() {
        val colorPair = preferenceApplier.colorPair()
        ToolbarColorApplier()(binding.toolbar, colorPair)
        WindowOptionColorApplier()(window, colorPair)

        RecentAppColoringUseCase(
                ::getString,
                { resources },
                ::setTaskDescription,
                Build.VERSION.SDK_INT
        ).invoke(preferenceApplier.color)

        menuSwitchColorApplier(colorPair)

        backgroundImageLoaderUseCase.invoke(binding.background, preferenceApplier.backgroundImagePath)

        updateColorFilter()
    }

    private fun updateColorFilter() {
        ForegroundColorFilterUseCase(preferenceApplier).invoke(binding.foreground)
        floatingPreview?.onResume()
    }

    /**
     * Open PDF from storage.
     */
    private fun openPdfTabFromStorage() {
        requestPermissionForOpenPdfTab.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    /**
     * Open Editor tab.
     */
    private fun openEditorTab(path: String? = null) {
        tabs.openNewEditorTab(path)
        replaceToCurrentTab()
    }

    /**
     * Switch tab list visibility.
     */
    private fun switchTabList() {
        if (tabListUseCase == null) {
            tabListUseCase = TabListUseCase(supportFragmentManager, this::refreshThumbnail)
        }
        tabListUseCase?.switch()
    }

    /**
     * Action on empty tabs.
     */
    private fun onEmptyTabs() {
        tabListUseCase?.dismiss()
        openNewTab()
    }

    private fun onClickClear() {
        tabs.clear()
        onEmptyTabs()
    }

    override fun onCloseOnly() {
        tabListUseCase?.dismiss()
    }

    override fun onCloseTabListDialogFragment(lastTabId: String) {
        if (lastTabId != tabs.currentTabId()) {
            replaceToCurrentTab()
        }
    }

    override fun onOpenEditor() = openEditorTab()

    override fun onOpenPdf() = openPdfTabFromStorage()

    override fun openNewTabFromTabList() {
        openNewTab()
    }

    private fun openNewTab() {
        when (StartUp.findByName(preferenceApplier.startUp)) {
            StartUp.SEARCH -> {
                replaceFragment(obtainFragment(SearchFragment::class.java))
            }
            StartUp.BROWSER -> {
                tabs.openNewWebTab()
                replaceToCurrentTab(true)
            }
            StartUp.BOOKMARK -> {
                replaceFragment(obtainFragment(BookmarkFragment::class.java))
            }
        }
    }

    override fun tabIndexFromTabList() = tabs.index()

    override fun currentTabIdFromTabList() = tabs.currentTabId()

    override fun replaceTabFromTabList(tab: Tab) {
        tabs.replace(tab)
        (obtainFragment(BrowserFragment::class.java) as? BrowserFragment)?.stopSwipeRefresherLoading()
    }

    override fun getTabByIndexFromTabList(position: Int): Tab? = tabs.getTabByIndex(position)

    override fun closeTabFromTabList(position: Int) {
        tabs.closeTab(position)
        (obtainFragment(BrowserFragment::class.java) as? BrowserFragment)?.stopSwipeRefresherLoading()
    }

    override fun getTabAdapterSizeFromTabList(): Int = tabs.size()

    override fun swapTabsFromTabList(from: Int, to: Int) = tabs.swap(from, to)

    override fun tabIndexOfFromTabList(tab: Tab): Int = tabs.indexOf(tab)

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_fab_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.open_tabs -> {
            switchTabList()
            true
        }
        R.id.setting -> {
            (obtainFragment(SettingFragment::class.java) as? SettingFragment)?.let {
                val currentFragment = findFragment()
                it.setFrom(currentFragment?.javaClass)
                replaceFragment(it, withAnimation = true, withSlideIn = true)
            }
            true
        }
        R.id.reset_menu_position -> {
            menuViewModel?.resetPosition()
            true
        }
        R.id.menu_exit -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        floatingPreview?.onPause()
        tabs.saveTabList()
    }

    override fun onDestroy() {
        tabs.dispose()
        disposables.cancel()
        searchWithClip.dispose()
        pageSearchPresenter.dispose()
        floatingPreview?.dispose()
        GlobalWebViewPool.dispose()
        activityResultLauncher?.unregister()
        requestPermissionForOpenPdfTab.unregister()
        downloadPermissionRequestLauncher.unregister()
        unregisterReceiver(musicPlayerBroadcastReceiver)
        supportFragmentManager.clearFragmentResultListener("clear_tabs")
        super.onDestroy()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_main

    }

}
