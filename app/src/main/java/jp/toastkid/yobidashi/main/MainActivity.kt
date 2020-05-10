package jp.toastkid.yobidashi.main

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.about.AboutThisAppFragment
import jp.toastkid.yobidashi.barcode.BarcodeReaderFragment
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.BrowserFragmentViewModel
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.ScreenMode
import jp.toastkid.yobidashi.browser.bookmark.BookmarkFragment
import jp.toastkid.yobidashi.browser.floating.FloatingPreview
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.databinding.ActivityMainBinding
import jp.toastkid.yobidashi.editor.EditorFragment
import jp.toastkid.yobidashi.launcher.LauncherFragment
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.ThumbnailGenerator
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.permission.RuntimePermissions
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier
import jp.toastkid.yobidashi.main.content.ContentViewModel
import jp.toastkid.yobidashi.menu.MenuBinder
import jp.toastkid.yobidashi.menu.MenuUseCase
import jp.toastkid.yobidashi.menu.MenuViewModel
import jp.toastkid.yobidashi.pdf.PdfViewerFragment
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchFragment
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.favorite.AddingFavoriteSearchService
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingFragment
import jp.toastkid.yobidashi.settings.fragment.OverlayColorFilterViewModel
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import jp.toastkid.yobidashi.tab.tab_list.TabListClearDialogFragment
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment
import jp.toastkid.yobidashi.tab.tab_list.TabListViewModel
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

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
     * Disposables.
     */
    private val disposables: Job by lazy { Job() }

    /**
     * Tab list dialog fragment.
     */
    private var tabListDialogFragment: DialogFragment? = null

    /**
     * Find-in-page module.
     */
    private lateinit var pageSearchPresenter: PageSearcherModule

    /**
     * Menu's view model.
     */
    private var menuViewModel: MenuViewModel? = null

    private var contentViewModel: ContentViewModel? = null

    private var tabListViewModel: TabListViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

    private var browserFragmentViewModel: BrowserFragmentViewModel? = null

    private var floatingPreview: FloatingPreview? = null

    private lateinit var tabs: TabAdapter

    /**
     * Search-with-clip object.
     */
    private lateinit var searchWithClip: SearchWithClip

    /**
     * Runtime permission.
     */
    private var runtimePermissions: RuntimePermissions? = null

    private val thumbnailGenerator = ThumbnailGenerator()

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var menuUseCase: MenuUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)

        setSupportActionBar(binding.toolbar)

        runtimePermissions = RuntimePermissions(this)

        val colorPair = preferenceApplier.colorPair()

        searchWithClip = SearchWithClip(
                applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
                binding.content,
                colorPair,
                browserViewModel
        )
        searchWithClip.invoke()

        pageSearchPresenter = PageSearcherModule(binding.sip)

        initializeHeaderViewModel()

        initializeMenuViewModel()

        initializeContentViewModel()

        browserViewModel = ViewModelProvider(this).get(BrowserViewModel::class.java)
        browserViewModel?.preview?.observe(this, Observer {
            Inputs.hideKeyboard(binding.content)

            if (floatingPreview == null) {
                floatingPreview = FloatingPreview(this)
            }
            floatingPreview?.show(binding.root, it.toString())
        })
        browserViewModel?.open?.observe(this, Observer(::openNewWebTab))
        browserViewModel?.openBackground?.observe(this, Observer {
            tabs.openBackgroundTab(it.toString(), it.toString())
            Toaster.snackShort(
                    binding.content,
                    getString(R.string.message_tab_open_background, it.toString()),
                    preferenceApplier.colorPair()
            )
        })
        browserViewModel?.openBackgroundWithTitle?.observe(this, Observer {
            tabs.openBackgroundTab(it.first, it.second.toString())
            Toaster.snackShort(
                    binding.content,
                    getString(R.string.message_tab_open_background, it.first),
                    preferenceApplier.colorPair()
            )
        })

        ViewModelProvider(this).get(LoadingViewModel::class.java)
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

        ViewModelProvider(this).get(OverlayColorFilterViewModel::class.java)
                .newColor
                .observe(this, Observer {
                    updateColorFilter()
                })

        tabListViewModel = ViewModelProvider(this).get(TabListViewModel::class.java)
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

        browserFragmentViewModel = ViewModelProvider(this).get(BrowserFragmentViewModel::class.java)

        tabs = TabAdapter({ this }, this::onEmptyTabs)

        processShortcut(intent)

        supportFragmentManager.addOnBackStackChangedListener {
            val findFragment = findFragment()

            if (findFragment !is TabUiFragment && supportFragmentManager.backStackEntryCount == 0) {
                moveTaskToBack(true)
            }
        }
    }

    private fun obtainFragment(fragmentClass: Class<out Fragment>) =
            supportFragmentManager.findFragmentByTag(fragmentClass.canonicalName)
                    ?: fragmentClass.newInstance()

    private fun initializeHeaderViewModel() {
        val headerViewModel = ViewModelProvider(this).get(HeaderViewModel::class.java)
        headerViewModel.content.observe(this, Observer { view ->
            if (view == null) {
                return@Observer
            }
            binding.toolbarContent.removeAllViews()
            binding.toolbar.layoutParams.height = view.layoutParams.height
            binding.toolbarContent.addView(view, 0)
        })

        headerViewModel.visibility.observe(this, Observer { isVisible ->
            if (isVisible) showToolbar() else hideToolbar()
        })
    }

    private fun initializeMenuViewModel() {
        menuViewModel = ViewModelProvider(this).get(MenuViewModel::class.java)

        MenuBinder(this, menuViewModel, binding.menuStub, binding.menuSwitch)

        menuUseCase = MenuUseCase({ this }, menuViewModel)
    }

    private fun initializeContentViewModel() {
        contentViewModel = ViewModelProvider(this).get(ContentViewModel::class.java)
        contentViewModel?.fragmentClass?.observe(this, Observer {
            replaceFragment(obtainFragment(it), withAnimation = true, withSlideIn = true)
        })
        contentViewModel?.fragment?.observe(this, Observer {
            replaceFragment(it, withAnimation = true, withSlideIn = false)
        })
        contentViewModel?.snackbar?.observe(this, Observer {
            Toaster.snackShort(binding.content, it, preferenceApplier.colorPair())
        })
        contentViewModel?.snackbarRes?.observe(this, Observer {
            Toaster.snackShort(binding.content, it, preferenceApplier.colorPair())
        })
        contentViewModel?.toTop?.observe(this, Observer {
            (findFragment() as? ContentScrollable)?.toTop()
        })
        contentViewModel?.toBottom?.observe(this, Observer {
            (findFragment() as? ContentScrollable)?.toBottom()
        })
        contentViewModel?.share?.observe(this, Observer {
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
        if (calledIntent.getBooleanExtra("random_wikipedia", false)) {
            RandomWikipedia().fetchWithAction { title, uri ->
                openNewWebTab(uri)
                Toaster.snackShort(
                        binding.content,
                        getString(R.string.message_open_random_wikipedia, title),
                        preferenceApplier.colorPair()
                )
            }
            return
        }

        when (calledIntent.action) {
            Intent.ACTION_VIEW -> {
                calledIntent.data?.let { openNewWebTab(it) }
                return
            }
            Intent.ACTION_SEND -> {
                calledIntent.extras?.getCharSequence(Intent.EXTRA_TEXT)?.also {
                    val query = it.toString()
                    if (Urls.isInvalidUrl(query)) {
                        search(preferenceApplier.getDefaultSearchEngine(), query)
                        return
                    }
                    openNewWebTab(query.toUri())
                }
                return
            }
            Intent.ACTION_WEB_SEARCH -> {
                val category = if (calledIntent.hasExtra(AddingFavoriteSearchService.EXTRA_KEY_CATEGORY)) {
                    calledIntent.getStringExtra(AddingFavoriteSearchService.EXTRA_KEY_CATEGORY)
                } else {
                    preferenceApplier.getDefaultSearchEngine()
                }
                search(category, calledIntent.getStringExtra(SearchManager.QUERY))
                return
            }
            BOOKMARK -> {
                replaceFragment(obtainFragment(BookmarkFragment::class.java))
            }
            APP_LAUNCHER -> {
                replaceFragment(obtainFragment(LauncherFragment::class.java))
            }
            BARCODE_READER -> {
                replaceFragment(obtainFragment(BarcodeReaderFragment::class.java))
            }
            SEARCH -> {
                replaceFragment(obtainFragment(SearchFragment::class.java))
            }
            SETTING -> {
                replaceFragment(obtainFragment(SettingFragment::class.java))
            }
            else -> {
                if (tabs.isEmpty()) {
                    openNewTab()
                    return
                }

                // Add for re-creating activity.
                val currentFragment = supportFragmentManager.findFragmentById(R.id.content)
                if (currentFragment is TabUiFragment || currentFragment == null) {
                    replaceToCurrentTab(false)
                }
            }
        }
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
    private fun replaceFragment(fragment: Fragment, withAnimation: Boolean = true, withSlideIn: Boolean = false) {
        val currentFragment = findFragment()
        if (currentFragment == fragment) {
            if (fragment is EditorFragment) {
                fragment.reload()
            }
            return
        }

        val fragments = supportFragmentManager.fragments
        if (fragments.size != 0 && fragments.contains(fragment)) {
            fragments.remove(fragment)
        }

        val transaction = supportFragmentManager.beginTransaction()
        if (withAnimation) {
            transaction.setCustomAnimations(
                    if (withSlideIn) R.anim.slide_in_right else R.anim.slide_up,
                    0,
                    0,
                    if (withSlideIn) android.R.anim.slide_out_right else R.anim.slide_down
            )
        }

        transaction.replace(R.id.content, fragment, fragment::class.java.canonicalName)

        if (fragment !is TabUiFragment) {
            transaction.addToBackStack(fragment::class.java.canonicalName)
        }
        transaction.commitAllowingStateLoss()
    }

    /**
     * Replace visibilities for current tab.
     *
     * @param withAnimation for suppress redundant animation.
     */
    private fun replaceToCurrentTab(withAnimation: Boolean = true) {
        when (val currentTab = tabs.currentTab()) {
            is WebTab -> {
                val browserFragment =
                        (obtainFragment(BrowserFragment::class.java) as? BrowserFragment) ?: return
                replaceFragment(browserFragment, false)
                browserFragmentViewModel
                        ?.loadWithNewTab(currentTab.getUrl().toUri() to currentTab.id())
            }
            is EditorTab -> {
                val editorFragment =
                        obtainFragment(EditorFragment::class.java) as? EditorFragment ?: return
                editorFragment.arguments = bundleOf("path" to currentTab.path)
                replaceFragment(editorFragment, withAnimation)
                refreshThumbnail()
            }
            is PdfTab -> {
                val url: String = currentTab.getUrl()
                if (url.isNotEmpty()) {
                    try {
                        val uri = Uri.parse(url)

                        val pdfViewerFragment =
                                obtainFragment(PdfViewerFragment::class.java) as? PdfViewerFragment ?: return
                        pdfViewerFragment.arguments = bundleOf("uri" to uri, "scrollY" to currentTab.getScrolled())
                        replaceFragment(pdfViewerFragment, withAnimation)
                        refreshThumbnail()
                    } catch (e: SecurityException) {
                        Timber.e(e)
                        return
                    } catch (e: IllegalStateException) {
                        Timber.e(e)
                        return
                    }
                }
            }
        }

        tabs.saveTabList()
    }

    /**
     * Show tab list.
     */
    private fun showTabList() {
        refreshThumbnail()
        // TODO Remove unused elvis operator.
        val fragmentManager = supportFragmentManager ?: return
        tabListDialogFragment?.show(fragmentManager, TabListDialogFragment::class.java.canonicalName)
    }

    private fun refreshThumbnail() {
        val findFragment = findFragment()
        if (findFragment !is TabUiFragment) {
            return
        }
        tabs.saveNewThumbnailAsync { thumbnailGenerator(binding.content) }
    }

    override fun onBackPressed() {
        if (tabListDialogFragment?.isVisible == true) {
            tabListDialogFragment?.dismiss()
            return
        }

        if (binding.menuStub.root?.isVisible == true) {
            menuViewModel?.close()
            return
        }

        if (pageSearchPresenter.isVisible()) {
            pageSearchPresenter.hide()
            return
        }

        val currentFragment = findFragment()
        if (currentFragment is CommonFragmentAction && currentFragment.pressBack()) {
            return
        }

        if (currentFragment is BrowserFragment || currentFragment is PdfViewerFragment) {
            tabs.closeTab(tabs.index())

            if (tabs.isEmpty()) {
                onEmptyTabs()
                return
            }
            replaceToCurrentTab(true)
            return
        }

        val fragment = findFragment()
        if (fragment !is EditorFragment) {
            supportFragmentManager.popBackStackImmediate()
            return
        }

        confirmExit()
    }

    private fun findFragment() = supportFragmentManager.findFragmentById(R.id.content)

    /**
     * Show confirm exit.
     */
    private fun confirmExit() {
        CloseDialogFragment()
                .show(supportFragmentManager, CloseDialogFragment::class.java.simpleName)
    }

    override fun onResume() {
        super.onResume()
        refresh()
        menuViewModel?.onResume()

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
        ToolbarColorApplier()(window, binding.toolbar, colorPair)
        binding.toolbar.backgroundTint = ColorStateList.valueOf(colorPair.bgColor())

        applyBackgrounds()

        updateColorFilter()
    }

    private fun updateColorFilter() {
        binding.foreground.foreground =
                if (preferenceApplier.useColorFilter()) ColorDrawable(preferenceApplier.filterColor())
                else null
    }

    /**
     * Apply background appearance.
     */
    private fun applyBackgrounds() {
        val backgroundImagePath = preferenceApplier.backgroundImagePath
        if (backgroundImagePath.isEmpty()) {
            binding.background.setImageDrawable(null)
            return
        }

        Glide.with(this)
                .load(File(backgroundImagePath).toURI().toString().toUri())
                .into(binding.background)
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

    private fun hideToolbar() {
        when (preferenceApplier.browserScreenMode()) {
            ScreenMode.FIXED -> Unit
            ScreenMode.FULL_SCREEN -> {
                binding.toolbar.visibility = View.GONE
            }
            ScreenMode.EXPANDABLE -> {
                binding.toolbar.animate()?.let {
                    it.cancel()
                    it.translationY(-resources.getDimension(R.dimen.toolbar_height))
                            .setDuration(HEADER_HIDING_DURATION)
                            .withStartAction { binding.content.requestLayout() }
                            .withEndAction   {
                                binding.toolbar.visibility = View.GONE
                            }
                            .start()
                }
            }
        }
    }

    private fun showToolbar() {
        when (preferenceApplier.browserScreenMode()) {
            ScreenMode.FIXED -> {
                binding.toolbar.visibility = View.VISIBLE
            }
            ScreenMode.FULL_SCREEN -> Unit
            ScreenMode.EXPANDABLE -> binding.toolbar.animate()?.let {
                it.cancel()
                it.translationY(0f)
                        .setDuration(HEADER_HIDING_DURATION)
                        .withStartAction {
                            binding.toolbar.visibility = View.VISIBLE
                        }
                        .withEndAction   { binding.content.requestLayout() }
                        .start()
            }
        }
    }

    /**
     * Switch tab list visibility.
     */
    fun switchTabList() {
        initTabListIfNeed()
        if (tabListDialogFragment?.isVisible == true) {
            tabListDialogFragment?.dismiss()
        } else {
            showTabList()
        }
    }

    /**
     * Initialize tab list.
     */
    private fun initTabListIfNeed() {
        tabListDialogFragment = TabListDialogFragment()
    }

    /**
     * Action on empty tabs.
     */
    private fun onEmptyTabs() {
        tabListDialogFragment?.dismiss()
        openNewTab()
    }

    override fun onClickClear() {
        tabs.clear()
        onEmptyTabs()
    }

    override fun onCloseOnly() {
        tabListDialogFragment?.dismiss()
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
        when (preferenceApplier.startUp) {
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
        menuInflater.also {
            it.inflate(R.menu.main_fab_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.open_tabs -> {
            switchTabList()
            true
        }
        R.id.setting -> {
            replaceFragment(obtainFragment(SettingFragment::class.java))
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
            moveTaskToBack(true)
            true
        }
        else -> super.onOptionsItemSelected(item)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    val takeFlags: Int = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver?.takePersistableUriPermission(uri, takeFlags)
                }

                tabs.openNewPdfTab(uri)
                replaceToCurrentTab(true)
                tabListDialogFragment?.dismiss()
            }
            VoiceSearch.REQUEST_CODE -> {
                VoiceSearch.processResult(this, data)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        tabs.saveTabList()
    }

    override fun onDestroy() {
        tabs.dispose()
        disposables.cancel()
        searchWithClip.dispose()
        pageSearchPresenter.dispose()
        floatingPreview?.dispose()
        super.onDestroy()
    }

    companion object {

        /**
         * Header hiding duration.
         */
        private const val HEADER_HIDING_DURATION = 75L

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
