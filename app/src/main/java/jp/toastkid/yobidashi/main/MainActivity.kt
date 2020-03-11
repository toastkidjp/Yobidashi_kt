package jp.toastkid.yobidashi.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
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
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.about.AboutThisAppFragment
import jp.toastkid.yobidashi.browser.*
import jp.toastkid.yobidashi.browser.bookmark.BookmarkFragment
import jp.toastkid.yobidashi.browser.history.ViewHistoryActivity
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.cleaner.ProcessCleanerInvoker
import jp.toastkid.yobidashi.databinding.ActivityMainBinding
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.editor.EditorFragment
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.ThumbnailGenerator
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.DraggableTouchListener
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier
import jp.toastkid.yobidashi.main.content.ContentSwitchOrder
import jp.toastkid.yobidashi.main.content.ContentViewModel
import jp.toastkid.yobidashi.menu.MenuBinder
import jp.toastkid.yobidashi.menu.MenuUseCase
import jp.toastkid.yobidashi.menu.MenuViewModel
import jp.toastkid.yobidashi.pdf.PdfViewerFragment
import jp.toastkid.yobidashi.rss.setting.RssSettingFragment
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.favorite.AddingFavoriteSearchService
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingsActivity
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import jp.toastkid.yobidashi.tab.tab_list.TabListClearDialogFragment
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment
import jp.toastkid.yobidashi.tab.tab_list.TabListViewModel
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import timber.log.Timber
import java.io.File
import kotlin.math.min

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
    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

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

    private lateinit var tabs: TabAdapter

    /**
     * Search-with-clip object.
     */
    private lateinit var searchWithClip: SearchWithClip

    /**
     * Rx permission.
     */
    private var rxPermissions: RxPermissions? = null

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

        rxPermissions = RxPermissions(this)

        val colorPair = preferenceApplier.colorPair()

        searchWithClip = SearchWithClip(
                applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
                binding.root,
                colorPair,
                browserViewModel
        )
        searchWithClip.invoke()

        setFabListener()

        pageSearchPresenter = PageSearcherModule(
                this,
                binding.sip as ModuleSearcherBinding
        )

        initializeHeaderViewModel()

        initializeMenuViewModel()

        contentViewModel = ViewModelProviders.of(this).get(ContentViewModel::class.java)
        contentViewModel?.content?.observe(this, Observer {
            when (it) {
                ContentSwitchOrder.RSS_SETTING -> {
                    val fragment = obtainFragment(RssSettingFragment::class.java)
                    replaceFragment(fragment)
                }
                null -> Unit
            }
        })

        browserViewModel = ViewModelProviders.of(this).get(BrowserViewModel::class.java)
        browserViewModel?.preview?.observe(this, Observer {
            (obtainFragment(BrowserFragment::class.java) as? BrowserFragment)?.preview(it.toString())
        })
        browserViewModel?.open?.observe(this, Observer {
            tabs.openNewWebTab(it.toString())
            replaceToCurrentTab(true)
        })
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

        ViewModelProviders.of(this).get(LoadingViewModel::class.java)
                .onPageFinished
                .observe(
                        this,
                        Observer { tabs.updateWebTab(it) }
                )

        tabListViewModel = ViewModelProviders.of(this).get(TabListViewModel::class.java)
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

        browserFragmentViewModel = ViewModelProviders.of(this).get(BrowserFragmentViewModel::class.java)

        tabs = TabAdapter({ this }, this::onEmptyTabs)

        processShortcut(intent)
    }

    private fun obtainFragment(fragmentClass: Class<out Fragment>) =
            supportFragmentManager.findFragmentByTag(fragmentClass.canonicalName)
                    ?: fragmentClass.newInstance()

    private fun initializeHeaderViewModel() {
        val headerViewModel = ViewModelProviders.of(this).get(HeaderViewModel::class.java)
        headerViewModel.content.observe(this, Observer { view ->
            if (view == null) {
                return@Observer
            }
            binding.toolbarContent.removeAllViews()
            binding.toolbar.layoutParams.height = view.layoutParams.height
            binding.toolbarContent.addView(
                    view,
                    0
            )
        })

        headerViewModel.visibility.observe(this, Observer { isVisible ->
            if (isVisible) showToolbar() else hideToolbar()
        })
    }

    private fun initializeMenuViewModel() {
        menuViewModel = ViewModelProviders.of(this).get(MenuViewModel::class.java)

        MenuBinder(
                this,
                menuViewModel,
                binding.menusView,
                binding.menuSwitch
        )

        menuUseCase = MenuUseCase(
                { this },
                { findCurrentFragment() },
                { replaceFragment(obtainFragment(it), true, false) },
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        ProcessCleanerInvoker()(binding.root).addTo(disposables)
                    }
                },
                { openPdfTabFromStorage() },
                { openEditorTab() },
                { pageSearchPresenter.switch() },
                { menuViewModel?.close() }
        )

        menuViewModel?.click?.observe(this, Observer(menuUseCase::onMenuClick))

        menuViewModel?.longClick?.observe(this, Observer {
            menuUseCase.onMenuLongClick(it)
        })
    }

    /**
     * Set FAB's listener.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setFabListener() {
        val listener = DraggableTouchListener()
        listener.setCallback(object : DraggableTouchListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                preferenceApplier.setNewMenuFabPosition(x, y)
            }
        })

        binding.menuSwitch.setOnTouchListener(listener)

        binding.menuSwitch.viewTreeObserver.addOnGlobalLayoutListener {
            val menuFabPosition = preferenceApplier.menuFabPosition()
            val displayMetrics = binding.menuSwitch.context.resources.displayMetrics
            if (binding.menuSwitch.x > displayMetrics.widthPixels) {
                binding.menuSwitch.x =
                        min(menuFabPosition?.first ?: 0f, displayMetrics.widthPixels.toFloat())
            }
            if (binding.menuSwitch.y > displayMetrics.heightPixels) {
                binding.menuSwitch.y =
                        min(menuFabPosition?.second ?: 0f, displayMetrics.heightPixels.toFloat())
            }
        }
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
        if (calledIntent.action == null) {
            // Add for re-creating activity.
            replaceToCurrentTab()
            return
        }

        if (calledIntent.getBooleanExtra("random_wikipedia", false)) {
            RandomWikipedia().fetchWithAction { title, uri ->
                openNewWebTab(uri)
                Toaster.snackShort(
                        binding.root,
                        getString(R.string.message_open_random_wikipedia, title),
                        preferenceApplier.colorPair()
                )
            }
            return
        }

        when (calledIntent.action) {
            Intent.ACTION_VIEW -> {
                calledIntent.data?.let { loadUri(it) }
                return
            }
            Intent.ACTION_SEND -> {
                calledIntent.extras?.getCharSequence(Intent.EXTRA_TEXT)?.also {
                    loadUri(it.toString().toUri())
                }
                return
            }
            Intent.ACTION_WEB_SEARCH -> {
                val category = if (calledIntent.hasExtra(AddingFavoriteSearchService.EXTRA_KEY_CATEGORY)) {
                    calledIntent.getStringExtra(AddingFavoriteSearchService.EXTRA_KEY_CATEGORY)
                } else {
                    preferenceApplier.getDefaultSearchEngine()
                }

                SearchAction(this, category, calledIntent.getStringExtra(SearchManager.QUERY))
                        .invoke()
                        .addTo(disposables)
                return
            }
            "${BuildConfig.APPLICATION_ID}.bookmark" -> {
                replaceFragment(obtainFragment(BookmarkFragment::class.java))
            }
        }
    }

    private fun openNewWebTab(uri: Uri) {
        tabs.openNewWebTab(uri.toString())
        replaceToCurrentTab(true)
    }

    /**
     * Use camera permission with specified action.
     *
     * @param onGranted action
     */
    private fun useCameraPermission(onGranted: () -> Unit) {
        rxPermissions
                ?.request(Manifest.permission.CAMERA)
                ?.filter { it }
                ?.subscribe(
                        { onGranted() },
                        { Timber.e(it) }
                )
                ?.addTo(disposables)
    }

    /**
     * Load Uri.
     *
     * @param uri
     */
    private fun loadUri(uri: Uri) {
        tabs.openNewWebTab(uri.toString())
        replaceToCurrentTab()
    }

    /**
     * Replace with passed fragment.
     *
     * @param fragment {@link BaseFragment} instance
     */
    private fun replaceFragment(fragment: Fragment, withAnimation: Boolean = true, withSlideIn: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        val fragments = supportFragmentManager.fragments
        if (fragments.size != 0) {
            if (fragments.contains(fragment)) {
                fragments.remove(fragment)
            }
        }

        if (withAnimation) {
            transaction.setCustomAnimations(
                    if (withSlideIn) R.anim.slide_up else R.anim.slide_in_right,
                    0,
                    0,
                    if (withSlideIn) R.anim.slide_down else android.R.anim.slide_out_right
            )
        }

        transaction.replace(R.id.content, fragment, fragment::class.java.canonicalName)

        // TODO fix it
        if (fragment !is BrowserFragment && fragment !is EditorFragment) {
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
                val browserFragment = (obtainFragment(BrowserFragment::class.java) as? BrowserFragment) ?: return
                replaceFragment(browserFragment, false)
                browserFragmentViewModel
                        ?.loadWithNewTab(currentTab.getUrl().toUri() to currentTab.id())
            }
            is EditorTab -> {
                val editorFragment =
                        obtainFragment(EditorFragment::class.java) as? EditorFragment ?: return
                editorFragment.arguments = bundleOf("path" to currentTab.path)
                replaceFragment(editorFragment, withAnimation)
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
        tabs.saveNewThumbnailAsync { thumbnailGenerator(binding.content) }
    }

    /**
     * Show tab list.
     */
    private fun showTabList() {
        refreshThumbnail()
        val fragmentManager = supportFragmentManager ?: return
        tabListDialogFragment?.show(fragmentManager, TabListDialogFragment::class.java.canonicalName)
    }

    private fun refreshThumbnail() {
        val currentTab = tabs.currentTab()
        tabs.deleteThumbnail(currentTab?.thumbnailPath)
        tabs.saveNewThumbnailAsync { thumbnailGenerator(binding.content) }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?) = when (event?.keyCode) {
        KeyEvent.KEYCODE_BACK ->
            findCurrentFragment()?.pressLongBack() ?: super.onKeyLongPress(keyCode, event)
        else ->
            super.onKeyLongPress(keyCode, event)
    }

    override fun onBackPressed() {
        if (tabListDialogFragment?.isVisible == true) {
            tabListDialogFragment?.dismiss()
            return
        }

        if (binding.menusView.isVisible) {
            menuViewModel?.close()
            return
        }

        if (pageSearchPresenter.isVisible()) {
            pageSearchPresenter.hide()
            return
        }

        val findCurrentFragment = findCurrentFragment()
        if (findCurrentFragment?.pressBack() == true) {
            return
        }

        if (findCurrentFragment is BrowserFragment) {
            tabs.closeTab(tabs.index())
            tabListViewModel?.tabCount(tabs.size())

            if (tabs.isEmpty()) {
                onEmptyTabs()
                return
            }
            replaceToCurrentTab(true)
            return
        }

        val backStackEntryCount = supportFragmentManager?.backStackEntryCount ?: 0
        if (backStackEntryCount >= 1) {
            supportFragmentManager?.popBackStack()
            return
        }

        confirmExit()
    }

    /**
     * Find current fragment.
     *
     * @return fragment or null
     */
    private fun findCurrentFragment(): CommonFragmentAction? {
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.content)

        return if (fragment != null) fragment as? CommonFragmentAction else null
    }

    /**
     * Show confirm exit.
     */
    private fun confirmExit() {
        CloseDialogFragment()
                .show(supportFragmentManager, CloseDialogFragment::class.java.simpleName)
    }

    override fun onStart() {
        super.onStart()

        if (tabs.isEmpty()) {
            tabs.openNewWebTab(preferenceApplier.homeUrl)
        }

        replaceToCurrentTab(false)
    }

    override fun onResume() {
        super.onResume()
        refresh()
        menuViewModel?.onResume()
        setFabPosition()
        tabs.loadBackgroundTabsFromDirIfNeed()

        menuViewModel?.tabCount(tabs.size())
        tabListViewModel?.tabCount(tabs.size())

        ClippingUrlOpener(binding.root) { browserViewModel?.open(it) }
    }

    /**
     * Refresh toolbar and background.
     */
    private fun refresh() {
        val colorPair = preferenceApplier.colorPair()
        ToolbarColorApplier()(window, binding.toolbar, colorPair)
        binding.toolbar.backgroundTint = ColorStateList.valueOf(colorPair.bgColor())

        colorPair.applyReverseTo(binding.menuSwitch)

        applyBackgrounds()

        updateColorFilter()
    }

    // TODO move to viewModel.
    fun updateColorFilter() {
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
            setBackgroundImage(null)
            return
        }

        Maybe.fromCallable {
            ImageLoader.readBitmapDrawable(
                    this,
                    File(backgroundImagePath).toURI().toString().toUri()
            )
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setBackgroundImage) {
                    Timber.e(it)
                    Toaster.snackShort(
                            binding.root,
                            getString(R.string.message_failed_read_image),
                            preferenceApplier.colorPair()
                    )
                    preferenceApplier.removeBackgroundImagePath()
                    setBackgroundImage(null)
                }
                .addTo(disposables)
    }

    /**
     * Set background image.
     *
     * @param background nullable
     */
    private fun setBackgroundImage(background: BitmapDrawable?) {
        binding.background.setImageDrawable(background)
    }

    /**
     * Open PDF from storage.
     */
    private fun openPdfTabFromStorage() {
        rxPermissions
                ?.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                ?.subscribe(
                        { granted ->
                            if (!granted) {
                                return@subscribe
                            }
                            startActivityForResult(
                                    IntentFactory.makeOpenDocument("application/pdf"),
                                    REQUEST_CODE_OPEN_PDF
                            )
                        },
                        Timber::e
                )?.addTo(disposables)
    }

    /**
     * Open Editor tab.
     */
    private fun openEditorTab(path: String? = null) {
        rxPermissions
                ?.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ?.subscribe(
                        { granted ->
                            if (!granted) {
                                Toaster.tShort(this, R.string.message_requires_permission_storage)
                                return@subscribe
                            }
                            tabs.openNewEditorTab(path)
                            replaceToCurrentTab()
                        },
                        Timber::e
                )
                ?.addTo(disposables)
    }

    private fun setFabPosition() {
        binding.menuSwitch.let {
            val fabPosition = preferenceApplier.menuFabPosition() ?: return@let
            val displayMetrics = it.context.resources.displayMetrics
            val x = when {
                fabPosition.first > displayMetrics.widthPixels.toFloat() ->
                    displayMetrics.widthPixels.toFloat()
                fabPosition.first < 0 -> 0f
                else -> fabPosition.first
            }
            val y = when {
                fabPosition.second > displayMetrics.heightPixels.toFloat() ->
                    displayMetrics.heightPixels.toFloat()
                fabPosition.second < 0 -> 0f
                else -> fabPosition.second
            }
            it.animate().x(x).y(y).setDuration(10).start()
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
        tabs.openNewWebTab()
        replaceToCurrentTab(true)
    }

    override fun onClickClear() {
        tabs.clear()
        onEmptyTabs()
    }

    override fun onCloseOnly() {
        tabListDialogFragment?.dismiss()
    }

    override fun onCloseTabListDialogFragment() {
        replaceToCurrentTab()
        menuViewModel?.tabCount(tabs.size())
        tabListViewModel?.tabCount(tabs.size())
    }

    override fun onOpenEditor() = openEditorTab()

    override fun onOpenPdf() = openPdfTabFromStorage()

    override fun openNewTabFromTabList() {
        tabs.openNewWebTab()
        replaceToCurrentTab(true)
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

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.open_tabs -> {
            switchTabList()
            true
        }
        R.id.setting -> {
            startActivity(SettingsActivity.makeIntent(this))
            true
        }
        R.id.reset_menu_position -> {
            binding.menuSwitch.let {
                it.translationX = 0f
                it.translationY = 0f
                preferenceApplier.clearMenuFabPosition()
            }
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
            // TODO Delete it.
            ViewHistoryActivity.REQUEST_CODE, BookmarkFragment.REQUEST_CODE -> {
                data.data?.let { loadUri(it) }
            }
            IntentIntegrator.REQUEST_CODE -> {
                val result: IntentResult? =
                        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result?.contents == null) {
                    Toaster.snackShort(binding.root, "Cancelled", preferenceApplier.colorPair())
                    return
                }
                Toaster.snackLong(
                        binding.root,
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

                (obtainFragment(PdfViewerFragment::class.java) as? PdfViewerFragment)?.let {
                    it.arguments = bundleOf("uri" to uri)
                    replaceFragment(it)
                }
            }
            VoiceSearch.REQUEST_CODE -> {
                VoiceSearch.processResult(this, data).addTo(disposables)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        tabs.saveTabList()
    }

    override fun onDestroy() {
        tabs.dispose()
        disposables.clear()
        searchWithClip.dispose()
        menuUseCase.dispose()
        contentViewModel?.content?.removeObservers(this)
        pageSearchPresenter.dispose()
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

        /**
         * Make launcher intent.
         *
         * @param context
         * @return [Intent]
         */
        fun makeIntent(context: Context) = Intent(context, MainActivity::class.java)
                .also { it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }

        /**
         * Make browser intent.
         *
         * @param context
         * @param uri
         *
         * @return [Intent]
         */
        fun makeBrowserIntent(context: Context, uri: Uri) = Intent(context, MainActivity::class.java)
                .also {
                    it.action = Intent.ACTION_VIEW
                    it.data = uri
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

        fun makeRandomWikipediaIntent(context: Context) = Intent(context, MainActivity::class.java)
                .also {
                    it.action = Intent.ACTION_VIEW
                    it.putExtra("random_wikipedia", true)
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

        fun makeBookmarkIntent(context: Context) = Intent(context, MainActivity::class.java)
                .also {
                    it.action = "${BuildConfig.APPLICATION_ID}.bookmark"
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

    }

}
