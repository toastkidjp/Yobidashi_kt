package jp.toastkid.yobidashi.main

import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.FileExtractorFromUri
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.permission.RuntimePermissions
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.tab.TabUiFragment
import jp.toastkid.lib.view.ToolbarColorApplier
import jp.toastkid.lib.view.WindowOptionColorApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.about.AboutThisAppFragment
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.BrowserFragmentViewModel
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.bookmark.BookmarkFragment
import jp.toastkid.yobidashi.browser.floating.FloatingPreview
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.databinding.ActivityMainBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.image.BackgroundImageLoaderUseCase
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.main.launch.ElseCaseUseCase
import jp.toastkid.yobidashi.main.launch.LauncherIntentUseCase
import jp.toastkid.yobidashi.main.launch.RandomWikipediaUseCase
import jp.toastkid.yobidashi.menu.MenuBinder
import jp.toastkid.yobidashi.menu.MenuSwitchColorApplier
import jp.toastkid.yobidashi.menu.MenuUseCase
import jp.toastkid.yobidashi.menu.MenuViewModel
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchFragment
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingFragment
import jp.toastkid.yobidashi.settings.fragment.OverlayColorFilterViewModel
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.tab_list.TabListClearDialogFragment
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Main of this calendar app.
 *
 * @author toastkidjp
 */
class MainActivity : AppCompatActivity(),
        TabListClearDialogFragment.Callback,
        TabListDialogFragment.Callback
{

    /**
     * Data binding object.
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * Runtime permission.
     */
    private var runtimePermissions: RuntimePermissions? = null

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

        runtimePermissions = RuntimePermissions(this)

        fragmentReplacingUseCase = FragmentReplacingUseCase(supportFragmentManager)

        val colorPair = preferenceApplier.colorPair()

        pageSearchPresenter = PageSearcherModule(binding.sip)

        appBarVisibilityUseCase = AppBarVisibilityUseCase(binding.toolbar, preferenceApplier)

        initializeHeaderViewModel()

        initializeMenuViewModel()

        initializeContentViewModel()

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
        browserViewModel?.openBackground?.observe(this, Observer {
            val uri = it?.getContentIfNotHandled() ?: return@Observer
            tabs.openBackgroundTab(uri.toString(), uri.toString())
            Toaster.snackShort(
                    binding.content,
                    getString(R.string.message_tab_open_background, uri.toString()),
                    preferenceApplier.colorPair()
            )
        })
        browserViewModel?.openBackgroundWithTitle?.observe(this, Observer {
            val pair = it?.getContentIfNotHandled() ?: return@Observer
            tabs.openBackgroundTab(pair.first, pair.second.toString())
            Toaster.snackShort(
                    binding.content,
                    getString(R.string.message_tab_open_background, pair.first),
                    preferenceApplier.colorPair()
            )
        })

        invokeSearchWithClip(colorPair)

        activityViewModelProvider.get(LoadingViewModel::class.java)
                .onPageFinished
                .observe(
                        this,
                        Observer {
                            tabs.updateWebTab(it)
                            if (tabs.currentTabId() == it.first) {
                                refreshThumbnail()
                            }
                        }
                )

        activityViewModelProvider.get(OverlayColorFilterViewModel::class.java)
                .newColor
                .observe(this, Observer {
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

        tabs = TabAdapter({ this }, this::onEmptyTabs)

        tabReplacingUseCase = TabReplacingUseCase(
                tabs,
                ::obtainFragment,
                { fragment, animation -> replaceFragment(fragment, animation) },
                activityViewModelProvider.get(BrowserFragmentViewModel::class.java),
                ::refreshThumbnail,
                { runOnUiThread(it) },
                disposables
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
                browserViewModel
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

        headerViewModel.visibility.observe(this, Observer { isVisible ->
            if (isVisible) appBarVisibilityUseCase.show() else appBarVisibilityUseCase.hide()
        })
    }

    private fun initializeMenuViewModel() {
        menuViewModel = ViewModelProvider(this).get(MenuViewModel::class.java)

        MenuBinder(this, menuViewModel, binding.menuStub, binding.menuSwitch, ::openSetting)

        MenuUseCase({ this }, menuViewModel).observe()
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
                    View.OnClickListener { snackbarEvent.action() },
                    preferenceApplier.colorPair()
            )
        })
        contentViewModel?.snackbarRes?.observe(this, Observer {
            val messageId = it?.getContentIfNotHandled() ?: return@Observer
            Toaster.snackShort(binding.content, messageId, preferenceApplier.colorPair())
        })
        contentViewModel?.toTop?.observe(this, Observer {
            (findFragment() as? ContentScrollable)?.toTop()
        })
        contentViewModel?.toBottom?.observe(this, Observer {
            (findFragment() as? ContentScrollable)?.toBottom()
        })
        contentViewModel?.share?.observe(this, Observer {
            if (it.hasBeenHandled) {
                return@Observer
            }
            it.getContentIfNotHandled()
            (findFragment() as? CommonFragmentAction)?.share()
        })
        contentViewModel?.webSearch?.observe(this, Observer {
            when (val fragment = findFragment()) {
                is BrowserFragment ->
                    fragment.search()
                else ->
                    contentViewModel?.nextFragment(SearchFragment::class.java)
            }
        })
        contentViewModel?.openPdf?.observe(this, Observer {
            openPdfTabFromStorage()
        })
        contentViewModel?.openEditorTab?.observe(this, Observer {
            openEditorTab()
        })
        contentViewModel?.switchPageSearcher?.observe(this, Observer {
            pageSearchPresenter.switch()
        })
        contentViewModel?.switchTabList?.observe(this, Observer {
            it?.getContentIfNotHandled() ?: return@Observer
            switchTabList()
        })
        contentViewModel?.refresh?.observe(this, Observer {
            refresh()
        })
        contentViewModel?.newArticle?.observe(this, Observer {
            val titleAndOnBackground = it?.getContentIfNotHandled() ?: return@Observer
            tabs.openNewArticleTab(titleAndOnBackground.first, titleAndOnBackground.second)
            if (titleAndOnBackground.second) {
                contentViewModel?.snackShort(
                        getString(R.string.message_tab_open_background, titleAndOnBackground.first)
                )
            } else {
                replaceToCurrentTab()
            }
        })
        contentViewModel?.openArticleList?.observe(this, Observer {
            tabs.openArticleList()
            replaceToCurrentTab()
        })
        contentViewModel?.openCalendar?.observe(this, Observer {
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
                        this::openNewWebTab
                ) { id, param -> getString(id, param) },
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
        ClippingUrlOpener(binding.content) { browserViewModel?.open(it) }
    }

    /**
     * Refresh toolbar and background.
     */
    private fun refresh() {
        val colorPair = preferenceApplier.colorPair()
        ToolbarColorApplier()(binding.toolbar, colorPair)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WindowOptionColorApplier()(window, colorPair)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RecentAppColoringUseCase(
                    ::getString,
                    { resources },
                    ::setTaskDescription
            ).invoke(preferenceApplier.color)
        }

        menuSwitchColorApplier(colorPair)

        backgroundImageLoaderUseCase.invoke(binding.background, preferenceApplier.backgroundImagePath)

        updateColorFilter()
    }

    private fun updateColorFilter() {
        binding.foreground.foreground =
                if (preferenceApplier.useColorFilter()) ColorDrawable(preferenceApplier.filterColor(Color.TRANSPARENT))
                else null
    }

    /**
     * Open PDF from storage.
     */
    private fun openPdfTabFromStorage() {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            runtimePermissions
                    ?.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ?.receiveAsFlow()
                    ?.collect { permission ->
                        if (!permission.granted) {
                            return@collect
                        }

                        startActivityForResult(
                                IntentFactory.makeOpenDocument("application/pdf"),
                                REQUEST_CODE_OPEN_PDF
                        )
                    }
        }
    }

    /**
     * Open Editor tab.
     */
    private fun openEditorTab(path: String? = null) {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            runtimePermissions
                    ?.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ?.receiveAsFlow()
                    ?.collect { permission ->
                        if (!permission.granted) {
                            return@collect
                        }

                        tabs.openNewEditorTab(path)
                        replaceToCurrentTab()
                    }
        }
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

    override fun onClickClear() {
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
        R.id.reset_menu_position -> {
            menuViewModel?.resetPosition()
            true
        }
        R.id.about_this_app -> {
            replaceFragment(obtainFragment(AboutThisAppFragment::class.java))
            true
        }
        R.id.menu_exit -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun openSetting() {
        (obtainFragment(SettingFragment::class.java) as? SettingFragment)?.let {
            val currentFragment = findFragment()
            it.setFrom(currentFragment?.javaClass)
            replaceFragment(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        when (requestCode) {
            IntentIntegrator.REQUEST_CODE -> {
                val result: IntentResult? =
                        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result?.contents == null) {
                    Toaster.snackShort(binding.content, "Cancelled", preferenceApplier.colorPair())
                    return
                }
                Toaster.snackLong(
                        binding.content,
                        "Scanned: ${result.contents}",
                        R.string.clip,
                        View.OnClickListener { Clipboard.clip(this, result.contents) },
                        preferenceApplier.colorPair()
                )
            }
            REQUEST_CODE_OPEN_PDF -> {
                val uri = data.data ?: return
                val takeFlags: Int = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver?.takePersistableUriPermission(uri, takeFlags)

                tabs.openNewPdfTab(uri)
                replaceToCurrentTab(true)
                tabListUseCase?.dismiss()
            }
            VoiceSearch.REQUEST_CODE -> {
                VoiceSearch.processResult(this, data)
            }
        }
    }

    /**
     * Workaround appcompat-1.1.0 bug.
     * @link https://issuetracker.google.com/issues/141132133
     */
    override fun applyOverrideConfiguration(overrideConfiguration: Configuration) =
            when (Build.VERSION.SDK_INT) {
                in 21..22 -> Unit
                else -> super.applyOverrideConfiguration(overrideConfiguration)
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
        super.onDestroy()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_main

        /**
         * Request code of opening PDF.
         */
        private const val REQUEST_CODE_OPEN_PDF: Int = 7

    }

}
