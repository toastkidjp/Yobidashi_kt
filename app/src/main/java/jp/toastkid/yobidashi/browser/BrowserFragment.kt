package jp.toastkid.yobidashi.browser

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.processors.PublishProcessor
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.ArchivesActivity
import jp.toastkid.yobidashi.browser.bookmark.BookmarkActivity
import jp.toastkid.yobidashi.browser.history.ViewHistoryActivity
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.browser.tab.*
import jp.toastkid.yobidashi.databinding.FragmentBrowserBinding
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.databinding.ModuleTabListBinding
import jp.toastkid.yobidashi.editor.EditorModule
import jp.toastkid.yobidashi.libs.ActivityOptionsFactory
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.ToolbarAction
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingsActivity
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Internal browser fragment.
 *
 * @author toastkidjp
 */
class BrowserFragment : BaseFragment() {

    /**
     * Archive folder.
     */
    private lateinit var tabs: TabAdapter

    /**
     * WebTab list module.
     */
    private lateinit var tabListModule: TabListModule

    /**
     * Search-with-clip object.
     */
    private lateinit var searchWithClip: SearchWithClip

    /**
     * Editor area.
     */
    private lateinit var editor: EditorModule

    /**
     * Data binding object.
     */
    private var binding: FragmentBrowserBinding? = null

    /**
     * Find-in-page module.
     */
    private var pageSearcherModule: PageSearcherModule? = null

    /**
     * Toolbar action object.
     */
    private var toolbarAction: ToolbarAction? = null

    /**
     * PublishProcessor of title pair.
     */
    private val titleProcessor: PublishProcessor<TitlePair> = PublishProcessor.create<TitlePair>()

    /**
     * Composite disposer.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * For disabling busy show & hide animation.
     */
    private var lastAnimated: Long = 0L

    /**
     * Set consumer to titleProcessor.
     */
    var consumer: Consumer<TitlePair>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolbarAction = context as ToolbarAction?
    }

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentBrowserBinding>(
                inflater!!, R.layout.fragment_browser, container, false)
        binding?.fragment = this

        binding?.webViewContainer?.let {
            it.setOnRefreshListener { tabs.reload() }
            it.setOnChildScrollUpCallback { _, _ -> tabs.disablePullToRefresh() }
        }

        initMenus()
        initFooter()

        val colorPair = colorPair()

        val cm = context.applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        searchWithClip = SearchWithClip(
                cm,
                binding?.root as View,
                colorPair,
                { url -> tabs.loadWithNewTab(Uri.parse(url)) }
        )
        searchWithClip.invoke()

        editor = EditorModule(
                binding?.editor as ModuleEditorBinding,
                { intent, requestCode -> startActivityForResult(intent, requestCode) },
                { switchTabList() },
                { closeTabList() },
                { file ->
                    val currentTab = tabs.currentTab()
                    if (currentTab is EditorTab) {
                        currentTab.setFileInformation(file)
                        tabs.saveTabList()
                    }
                },
                { if (it) {
                    hideFooter()
                    binding?.fab?.hide()
                  } else showFooter() }
        )

        tabs = TabAdapter(
                binding?.progress as ProgressBar,
                binding?.webViewContainer as ViewGroup,
                editor,
                binding?.footer?.tabCount as TextView,
                { titleProcessor.onNext(it) },
                { binding?.webViewContainer?.isRefreshing = false },
                { this.hideOption() },
                { onScroll(it) },
                this::onEmptyTabs
        )

        tabListModule = TabListModule(
                DataBindingUtil.inflate<ModuleTabListBinding>(
                        LayoutInflater.from(activity), R.layout.module_tab_list, null, false),
                tabs,
                binding?.root as View,
                this::hideTabList,
                this::onEmptyTabs
        )

        pageSearcherModule = PageSearcherModule(binding?.sip as ModuleSearcherBinding, tabs)

        setHasOptionsMenu(true)

        return binding?.root
    }

    /**
     * Action on empty tabs.
     */
    private fun onEmptyTabs() {
        tabListModule.hide()
        fragmentManager.popBackStack()
    }

    /**
     * On scroll action.
     *
     * @param upward is scroll on upward
     */
    private fun onScroll(upward: Boolean) {

        val browserScreenMode = preferenceApplier().browserScreenMode()

        if (browserScreenMode == ScreenMode.FIXED) {
            return
        }

        if (upward) {
            binding?.fab?.show()
            if (browserScreenMode == ScreenMode.EXPANDABLE) {
                showFooter()
            }
        } else {
            binding?.fab?.hide()
            if (browserScreenMode == ScreenMode.EXPANDABLE) {
                hideFooter()
            }
        }
    }

    /**
     * Show footer with animation.
     */
    private fun showFooter() {
        if (disallowAnimation()) {
            return
        }
        lastAnimated = System.currentTimeMillis()

        binding?.footer?.root?.animate()?.let {
            it.cancel()
            it.translationY(0f)
                    ?.setDuration(ANIMATION_DURATION)
                    ?.withStartAction { binding?.footer?.root?.visibility = View.VISIBLE }
                    ?.withEndAction { toolbarAction?.showToolbar() }
                    ?.start()
        }
    }

    /**
     * Hide footer with animation.
     */
    private fun hideFooter() {
        if (disallowAnimation()) {
            return
        }
        lastAnimated = System.currentTimeMillis()
        binding?.footer?.root?.animate()?.let {
            it.cancel()
            it.translationY(resources.getDimension(R.dimen.browser_footer_height))
                    ?.setDuration(ANIMATION_DURATION)
                    ?.withEndAction {
                        toolbarAction?.hideToolbar()
                        binding?.footer?.root?.visibility = View.GONE
                    }
                    ?.start()
        }
    }

    /**
     * Check disallow header & footer's animation.
     */
    private fun disallowAnimation(): Boolean
            = (System.currentTimeMillis() - lastAnimated) < ALLOWABLE_INTERVAL_MS

    /**
     * Initialize footer.
     */
    private fun initFooter() {
        binding?.footer?.let {
            it.back.setOnClickListener { back() }
            it.forward.setOnClickListener { forward() }
            it.bookmark.setOnClickListener { bookmark(ActivityOptionsFactory.makeScaleUpBundle(it)) }
            it.search.setOnClickListener { search(ActivityOptionsFactory.makeScaleUpBundle(it)) }
            it.toTop.setOnClickListener { toTop() }
            it.toBottom.setOnClickListener { toBottom() }
            it.tabList.setOnClickListener { switchTabList() }
        }
    }

    /**
     * Initialize menus view.
     */
    private fun initMenus() {
        binding?.menusView?.adapter = Adapter(activity, Consumer<Menu> { this.onMenuClick(it) })
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding?.menusView?.layoutManager = layoutManager
        layoutManager.scrollToPosition(Adapter.mediumPosition())
    }

    /**
     * Switch menu visibility.
     */
    private fun switchMenu() {
        hideTabList()
        if (binding?.menusView?.visibility == View.GONE) {
            showMenu(binding?.root ?: View(context))
        } else {
            hideMenu()
        }
    }

    /**
     * Show quick control menu.
     *
     * @param ignored defined for Data-Binding
     */
    fun showMenu(ignored: View) {
        binding?.fab?.hide()
        binding?.menusView?.visibility = View.VISIBLE
    }

    /**
     * Hide quick control menu.
     */
    private fun hideMenu() {
        binding?.fab?.show()
        binding?.menusView?.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.browser, menu)

        menu?.let {
            it.findItem(R.id.open_menu)?.setOnMenuItemClickListener {
                switchMenu()
                true
            }

            it.findItem(R.id.open_tabs)?.setOnMenuItemClickListener {
                switchTabList()
                true
            }

            it.findItem(R.id.setting)?.setOnMenuItemClickListener {
                startActivity(SettingsActivity.makeIntent(context))
                true
            }

            it.findItem(R.id.stop_loading)?.setOnMenuItemClickListener {
                stopCurrentLoading()
                true
            }
        }
    }

    /**
     * Stop current tab's loading.
     */
    private fun stopCurrentLoading() {
        tabs.stopLoading()
        Toaster.snackShort(binding?.root as View, R.string.message_stop_loading, colorPair())
    }

    /**
     * Menu action.
     *
     * @param menu
     */
    private fun onMenuClick(menu: Menu) {
        val context = activity
        val snackbarParent = binding?.root as View
        when (menu) {
            Menu.RELOAD -> {
                tabs.reload()
            }
            Menu.BACK -> {
                back()
            }
            Menu.FORWARD -> {
                forward()
            }
            Menu.TOP -> {
                toTop()
            }
            Menu.BOTTOM -> {
                toBottom()
            }
            Menu.FIND_IN_PAGE -> {
                if (pageSearcherModule?.isVisible ?: false) {
                    pageSearcherModule?.hide()
                    return
                }
                pageSearcherModule?.show(activity)
                hideMenu()
            }
            Menu.SCREENSHOT -> {
                tabs.currentSnap()
                Toaster.snackShort(snackbarParent, R.string.message_done_save, colorPair())
            }
            Menu.SHARE -> {
                startActivity(
                        IntentFactory.makeShare(tabs.currentTitle()
                                + System.getProperty("line.separator") + tabs.currentUrl())
                )
            }
            Menu.SETTING -> {
                startActivity(SettingsActivity.makeIntent(context))
            }
            Menu.TAB_HISTORY -> {
                launchTabHistory(context)
            }
            Menu.USER_AGENT -> {
                UserAgent.showSelectionDialog(
                        snackbarParent,
                        { tabs.resetUserAgent(it.text()) }
                )
            }
            Menu.WIFI_SETTING -> {
                startActivity(SettingsIntentFactory.wifi())
            }
            Menu.PAGE_INFORMATION -> {
                tabs.showPageInformation()
            }
            Menu.TAB_LIST -> {
                switchTabList()
            }
            Menu.STOP_LOADING -> {
                stopCurrentLoading()
            }
            Menu.OPEN -> {
                val inputLayout = TextInputs.make(context)
                inputLayout.editText?.setText(tabs.currentUrl())
                AlertDialog.Builder(context)
                        .setTitle(R.string.title_open_url)
                        .setView(inputLayout)
                        .setCancelable(true)
                        .setPositiveButton(R.string.open) { d, i ->
                            val url = inputLayout.editText?.text.toString()
                            if (Urls.isValidUrl(url)) {
                                tabs.loadWithNewTab(Uri.parse(url))
                            }
                        }
                        .show()
            }
            Menu.OTHER_BROWSER -> {
                CustomTabsFactory.make(context, colorPair(), R.drawable.ic_back)
                        .build()
                        .launchUrl(context, Uri.parse(tabs.currentUrl()))
            }
            Menu.SHARE_BARCODE -> {
                SharingUrlByBarcode.invoke(context, tabs.currentUrl() ?: "")
            }
            Menu.ARCHIVE -> {
                tabs.saveArchive()
            }
            Menu.SEARCH -> {
                search(ActivityOptionsFactory.makeScaleUpBundle(binding?.menusView as View))
            }
            Menu.SITE_SEARCH -> {
                tabs.siteSearch()
            }
            Menu.VOICE_SEARCH -> {
                startActivityForResult(VoiceSearch.makeIntent(context), REQUEST_CODE_VOICE_SEARCH)
            }
            Menu.REPLACE_HOME -> {
                val currentUrl = tabs.currentUrl()
                currentUrl?.let {
                    if (Urls.isInvalidUrl(currentUrl)) {
                        Toaster.snackShort(
                                snackbarParent,
                                R.string.message_cannot_replace_home_url,
                                colorPair()
                        )
                        return
                    }
                    preferenceApplier().homeUrl = currentUrl
                    Toaster.snackShort(
                            snackbarParent,
                            getString(R.string.message_replace_home_url, currentUrl) ,
                            colorPair()
                    )
                }
            }
            Menu.LOAD_HOME -> {
                tabs.loadHome()
            }
            Menu.VIEW_HISTORY -> {
                startActivityForResult(
                        ViewHistoryActivity.makeIntent(context),
                        ViewHistoryActivity.REQUEST_CODE
                )
            }
            Menu.ADD_BOOKMARK -> {
                tabs.addBookmark {
                    bookmark(ActivityOptionsFactory.makeScaleUpBundle(binding?.menusView as View))
                }
            }
            Menu.EDITOR -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED
                            || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED
                        )
                        ) {
                    requestPermissions(arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ), 1)
                    return
                }
                openEditorTab()
            }
            Menu.EXIT -> {
                activity.finish()
            }
        }
    }

    /**
     * Launch current tab's history activity.
     *
     * @param context
     */
    private fun launchTabHistory(context: Context) {
        val scaleUpAnimation = ActivityOptions.makeScaleUpAnimation(
                binding?.menusView, 0, 0,
                binding?.menusView?.width ?: 0, binding?.menusView?.height ?: 0)
        val currentTab = tabs.currentTab()
        if (currentTab is WebTab) {
            startActivityForResult(
                    TabHistoryActivity.makeIntent(context, currentTab),
                    TabHistoryActivity.REQUEST_CODE,
                    scaleUpAnimation.toBundle()
            )
        }
    }

    /**
     * Initialize tab list.
     *
     * @param snackbarParent Snackbar's parent view.
     */
    private fun initTabListIfNeed(ignored: View) {
        if (binding?.tabListContainer?.childCount == 0) {
            binding?.tabListContainer?.addView(tabListModule.moduleView)
        }
    }

    /**
     * Switch tab list visibility.
     */
    private fun switchTabList() {
        initTabListIfNeed(binding?.root as View)
        if (tabListModule.isVisible ?: false) {
            hideTabList()
        } else {
            showTabList()
        }
    }

    /**
     * Close tab list module.
     */
    private inline fun closeTabList() {
        tabListModule.let { if (it.isVisible) { it.hide() } }
    }

    /**
     * Do browser back action.
     */
    private fun back(): Boolean {
        val back = tabs.back()
        if (back.isNotEmpty()) {
            tabs.loadUrl(back, false)
            return true
        }
        return false
    }

    /**
     * Do browser forward action.
     */
    private fun forward() {
        val forward = tabs.forward()
        if (forward.isNotEmpty()) {
            tabs.loadUrl(forward, false)
        }
    }

    /**
     * Show bookmark activity.
     *
     * @param option [ActivityOptions]
     */
    private fun bookmark(option: ActivityOptions) {
        startActivityForResult(
                BookmarkActivity.makeIntent(activity),
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
        startActivity(SearchActivity.makeIntent(context), option.toBundle())
    }

    /**
     * Go to bottom.
     */
    private fun toBottom() {
        tabs.pageDown()
    }

    /**
     * Go to top.
     */
    private fun toTop() {
        tabs.pageUp()
    }

    override fun onResume() {
        super.onResume()

        refreshFab()

        applyFooterColor(colorPair())
        editor.applyColor()

        disposables.add(tabs.reloadWebViewSettings())
        disposables.add(titleProcessor.subscribe(consumer))

        tabs.loadBackgroundTabsFromDirIfNeed()

        if (tabs.isNotEmpty()) {
            tabs.setCurrentTab()
            tabs.replaceToCurrentTab()
        } else {
            tabs.loadWithNewTab(Uri.parse(preferenceApplier().homeUrl))
        }

        val preferenceApplier = preferenceApplier()
        if (preferenceApplier.browserScreenMode() == ScreenMode.FULL_SCREEN
                || editor.isVisible) {
            hideFooter()
            return
        }

        binding?.webViewContainer?.let {
            it.setProgressBackgroundColorSchemeColor(preferenceApplier.color)
            it.setColorSchemeColors(preferenceApplier.fontColor)
        }

        showFooter()
    }

    /**
     * Apply footer color with [ColorPair].
     *
     * @param colorPair [ColorPair]
     */
    private fun applyFooterColor(colorPair: ColorPair) {
        binding?.footer?.let {
            val fontColor = colorPair.fontColor()
            it.root?.setBackgroundColor(colorPair.bgColor())
            it.back.setColorFilter(fontColor)
            it.forward.setColorFilter(fontColor)
            it.bookmark.setColorFilter(fontColor)
            it.search.setColorFilter(fontColor)
            it.toTop.setColorFilter(fontColor)
            it.toBottom.setColorFilter(fontColor)
            it.tabIcon.setColorFilter(fontColor)
            it.tabCount.setTextColor(fontColor)
        }
    }

    /**
     * Refresh fab.
     */
    private fun refreshFab() {
        val preferenceApplier = preferenceApplier() as PreferenceApplier
        binding?.fab?.setBackgroundColor(preferenceApplier.colorPair().bgColor())

        val resources = resources
        val fabMarginBottom = resources.getDimensionPixelSize(R.dimen.fab_margin)
        val fabMarginHorizontal = resources.getDimensionPixelSize(R.dimen.fab_margin_horizontal)
        MenuPos.place(binding?.fab as View, fabMarginBottom, fabMarginHorizontal, preferenceApplier.menuPos())
    }

    override fun pressLongBack(): Boolean {
        launchTabHistory(activity)
        return true
    }

    override fun pressBack(): Boolean = hideOption() || back()

    /**
     * Hide option menus.
     */
    private fun hideOption(): Boolean {
        if (tabListModule != null && tabListModule.isVisible as Boolean) {
            hideTabList()
            return true
        }

        if (binding?.menusView?.visibility == View.VISIBLE) {
            hideMenu()
            return true
        }
        return false
    }

    /**
     * Hide tab list.
     */
    private fun hideTabList() {
        tabListModule.hide()
        if (tabs.currentTab() is EditorTab) {
            return
        }
        binding?.fab?.show()
    }

    /**
     * Show tab list.
     */
    private fun showTabList() {
        hideMenu()
        binding?.fab?.hide()
        tabListModule.show()
    }

    override fun titleId(): Int = R.string.title_browser

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return
        }
        when (requestCode) {
            ArchivesActivity.REQUEST_CODE -> {
                loadArchive(File(intent.getStringExtra(ArchivesActivity.EXTRA_KEY_FILE_NAME)))
                return
            }
            REQUEST_CODE_VOICE_SEARCH -> {
                disposables.add(VoiceSearch.processResult(activity, intent))
                return
            }
            BookmarkActivity.REQUEST_CODE, ViewHistoryActivity.REQUEST_CODE -> {
                if (intent.data != null) { tabs.loadWithNewTab(intent.data) }
            }
            TabHistoryActivity.REQUEST_CODE -> {
                if (intent.hasExtra(TabHistoryActivity.EXTRA_KEY_INDEX)) {
                    tabs.moveTo(intent.getIntExtra(TabHistoryActivity.EXTRA_KEY_INDEX, 0))
                }
            }
            EditorModule.REQUEST_CODE_LOAD -> {
                editor.readFromFileUri(intent.data)
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openEditorTab()
            return
        }
        Toaster.tShort(activity, R.string.message_requires_permission_storage)
    }

    /**
     * Open editor tab.
     */
    private inline fun openEditorTab() {
        tabs.openNewEditorTab()
        tabs.replaceToCurrentTab()
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
        } catch (error: OutOfMemoryError) {
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
        tabs.loadWithNewTab(uri)
    }

    override fun onPause() {
        super.onPause()
        editor.saveIfNeed()
        binding?.tabListContainer?.removeAllViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        (binding?.menusView?.adapter as Adapter).dispose()
        tabs.dispose()
        disposables.dispose()
        searchWithClip.dispose()
        toolbarAction?.showToolbar()
    }

    companion object {

        /**
         * Request code of voice search.
         */
        private const val REQUEST_CODE_VOICE_SEARCH: Int = 2

        /**
         * Animation's dutarion.
         */
        private const val ANIMATION_DURATION: Long = 75L

        /**
         * Allowable interval milliseconds.
         */
        private const val ALLOWABLE_INTERVAL_MS: Long = 500L

    }

}
