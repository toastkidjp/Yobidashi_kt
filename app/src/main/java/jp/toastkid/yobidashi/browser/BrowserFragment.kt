package jp.toastkid.yobidashi.browser

import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import jp.toastkid.yobidashi.browser.tab.TabAdapter
import jp.toastkid.yobidashi.browser.tab.TabHistoryActivity
import jp.toastkid.yobidashi.browser.tab.TabListModule
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.FragmentBrowserBinding
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.databinding.ModuleTabListBinding
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

    /** Data binding object.  */
    private var binding: FragmentBrowserBinding? = null

    /** Archive folder.  */
    private lateinit var tabs: TabAdapter

    private var tabListModule: TabListModule? = null

    private var pageSearcherModule: PageSearcherModule? = null

    /** Title processor  */
    private val titleProcessor: PublishProcessor<TitlePair> = PublishProcessor.create<TitlePair>()

    /** Disposer.  */
    private val disposables: CompositeDisposable = CompositeDisposable()

    private lateinit var searchWithClip: SearchWithClip

    private var toolbarAction: ToolbarAction? = null

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

        tabs = TabAdapter(
                binding?.progress as ProgressBar,
                binding?.webViewContainer as FrameLayout,
                binding?.footer?.tabCount as TextView,
                { titleProcessor.onNext(it) },
                { binding?.refresher?.isRefreshing = false },
                { this.hideOption() },
                { onScroll(it) },
                { fragmentManager.popBackStack() }
        )

        binding?.refresher?.setOnRefreshListener({ tabs.reload() })
        binding?.refresher?.setOnChildScrollUpCallback { _, _ -> tabs.enablePullToRefresh() }
        initMenus()

        val colorPair = colorPair()
        initFooter(colorPair)

        pageSearcherModule = PageSearcherModule(binding?.sip as ModuleSearcherBinding, tabs)

        val cm = activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        searchWithClip = SearchWithClip(
                cm,
                binding?.root as View,
                colorPair,
                { url -> tabs.loadWithNewTab(Uri.parse(url)) }
        )
        searchWithClip.invoke()

        if (arguments != null && arguments.containsKey("url")) {
            val url = arguments.getParcelable<Uri>("url")
            if (url != null) {
                tabs.loadWithNewTab(url)
            }
        }

        setHasOptionsMenu(true)

        return binding?.root
    }

    /**
     * On scroll action.
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

    private fun showFooter() {
        val animate = binding?.footer?.root?.animate()
        animate?.cancel()
        animate?.translationY(0f)
                ?.setDuration(ANIMATION_DURATION)
                ?.withStartAction { binding?.footer?.root?.visibility = View.VISIBLE }
                ?.withEndAction { toolbarAction?.showToolbar() }
                ?.start()
    }

    private fun hideFooter() {
        val animate = binding?.footer?.root?.animate()
        animate?.cancel()
        animate?.translationY(resources.getDimension(R.dimen.browser_footer_height))
                ?.setDuration(ANIMATION_DURATION)
                ?.withEndAction {
                    toolbarAction?.hideToolbar()
                    binding?.footer?.root?.visibility = View.GONE
                }
                ?.start()
    }

    /**
     * Initialize footer with [ColorPair]
     *
     * @param colorPair [ColorPair]
     */
    private fun initFooter(colorPair: ColorPair) {
        binding?.footer?.root?.setBackgroundColor(colorPair.bgColor())
        val fontColor = colorPair.fontColor()
        binding?.footer?.back?.setColorFilter(fontColor)
        binding?.footer?.back?.setOnClickListener { back() }

        binding?.footer?.forward?.setColorFilter(fontColor)
        binding?.footer?.forward?.setOnClickListener { forward() }

        binding?.footer?.bookmark?.setColorFilter(fontColor)
        binding?.footer?.bookmark?.setOnClickListener { bookmark() }

        binding?.footer?.search?.setColorFilter(fontColor)
        binding?.footer?.search?.setOnClickListener { search() }

        binding?.footer?.toTop?.setColorFilter(fontColor)
        binding?.footer?.toTop?.setOnClickListener { toTop() }

        binding?.footer?.toBottom?.setColorFilter(fontColor)
        binding?.footer?.toBottom?.setOnClickListener { toBottom() }

        binding?.footer?.tabIcon?.setColorFilter(fontColor)
        binding?.footer?.tabCount?.setTextColor(fontColor)
        binding?.footer?.tab?.setOnClickListener { showTabListWithParent(binding?.root as View) }
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

    fun switchMenu() {
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
     * @param ignored
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

        menu?.findItem(R.id.open_menu)?.setOnMenuItemClickListener { v ->
            switchMenu()
            true
        }

        menu?.findItem(R.id.open_tabs)?.setOnMenuItemClickListener { v ->
            switchTabList()
            true
        }

        menu?.findItem(R.id.setting)?.setOnMenuItemClickListener { v ->
            startActivity(SettingsActivity.makeIntent(context))
            true
        }
    }

    /**
     * Menu action.
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
                showTabListWithParent(snackbarParent)
            }
            Menu.OPEN -> {
                val inputLayout = TextInputs.make(context)
                inputLayout.editText?.setText(tabs.currentUrl())
                AlertDialog.Builder(context)
                        .setTitle(R.string.title_open_url)
                        .setView(inputLayout)
                        .setCancelable(true)
                        .setPositiveButton("開く") { d, i ->
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
                SharingUrlByBarcode.invoke(context, tabs.currentUrl())
            }
            Menu.ARCHIVE -> {
                tabs.saveArchive()
            }
            Menu.SEARCH -> {
                search()
            }
            Menu.SITE_SEARCH -> {
                tabs.siteSearch()
            }
            Menu.VOICE_SEARCH -> {
                startActivityForResult(VoiceSearch.makeIntent(context), REQUEST_CODE_VOICE_SEARCH)
            }
            Menu.COLOR_FILTER -> {
                ColorFilter(activity, snackbarParent).switchState(this, REQUEST_OVERLAY_PERMISSION)
            }
            Menu.REPLACE_HOME -> {
                val currentUrl = tabs.currentUrl()
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
                tabs.addBookmark{
                    startActivityForResult(
                            BookmarkActivity.makeIntent(activity),
                            BookmarkActivity.REQUEST_CODE
                    )
                };
            }
            Menu.EXIT -> {
                activity.finish()
            }
        }
    }

    /**
     * Launch current tab's history activity.
     */
    private fun launchTabHistory(context: FragmentActivity) {
        val scaleUpAnimation = ActivityOptions.makeScaleUpAnimation(
                binding?.menusView, 0, 0,
                binding?.menusView?.width ?: 0, binding?.menusView?.height ?: 0)
        startActivityForResult(
                TabHistoryActivity.makeIntent(context, tabs.currentTab()),
                TabHistoryActivity.REQUEST_CODE,
                scaleUpAnimation.toBundle()
        )
    }

    private fun initTabListIfNeed(snackbarParent: View) {
        if (tabListModule == null) {
            tabListModule = TabListModule(
                    DataBindingUtil.inflate<ModuleTabListBinding>(
                            LayoutInflater.from(activity), R.layout.module_tab_list, null, false),
                    tabs,
                    snackbarParent,
                    this::hideTabList
            )
            binding?.tabListContainer?.addView(tabListModule?.moduleView)
        }
    }

    private fun switchTabList() {
        initTabListIfNeed(binding?.root as View)
        if (tabListModule?.isVisible ?: false) {
            hideTabList()
        } else {
            showTabList()
        }
    }

    private fun back(): Boolean {
        val back = tabs.back()
        if (back.isNotEmpty()) {
            tabs.loadUrl(back, false)
            return true
        }
        return false
    }

    private fun forward() {
        val forward = tabs.forward()
        if (forward.isNotEmpty()) {
            tabs.loadUrl(forward, false)
        }
    }

    private fun bookmark() {
        startActivityForResult(
                BookmarkActivity.makeIntent(activity),
                BookmarkActivity.REQUEST_CODE
        )
    }

    private fun search() {
        startActivity(SearchActivity.makeIntent(context))
    }

    private fun showTabListWithParent(snackbarParent: View) {
        initTabListIfNeed(snackbarParent)
        switchTabList()
    }

    private fun toBottom() {
        tabs.pageDown()
    }

    private fun toTop() {
        tabs.pageUp()
    }

    override fun onResume() {
        super.onResume()

        refreshFab()

        val preferenceApplier = preferenceApplier()
        if (preferenceApplier.browserScreenMode() == ScreenMode.FULL_SCREEN) {
            hideFooter()
        } else {
            showFooter()
        }

        disposables.add(tabs.reloadWebViewSettings())
    }

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

    private fun hideOption(): Boolean {
        if (tabListModule != null && tabListModule?.isVisible as Boolean) {
            hideTabList()
            return true
        }

        if (binding?.menusView?.visibility == View.VISIBLE) {
            hideMenu()
            return true
        }
        return false
    }

    private fun hideTabList() {
        if (tabs.size() == 0) {
            fragmentManager.popBackStack()
            return
        }
        tabListModule?.hide()
        binding?.fab?.show()
    }

    private fun showTabList() {
        hideMenu()
        binding?.fab?.hide()
        tabListModule?.show()
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
                if (intent.data != null) {tabs.loadUrl(intent.data.toString())}
            }
            TabHistoryActivity.REQUEST_CODE -> {
                if (intent.hasExtra(TabHistoryActivity.EXTRA_KEY_INDEX)) {
                    tabs.moveTo(intent.getIntExtra(TabHistoryActivity.EXTRA_KEY_INDEX, 0))
                }
            }
            REQUEST_OVERLAY_PERMISSION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                    Toaster.snackShort(
                            binding?.root as View,
                            R.string.message_cannot_draw_overlay,
                            colorPair()
                    )
                    return
                }
                ColorFilter(activity, binding?.root as View)
                        .switchState(this, REQUEST_OVERLAY_PERMISSION)
                return
            }
        }
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

    fun titlePairProcessor(): PublishProcessor<TitlePair> = titleProcessor

    override fun onDestroy() {
        super.onDestroy()
        (binding?.menusView?.adapter as Adapter).dispose()
        tabs.dispose()
        disposables.dispose()
        searchWithClip.dispose()
        toolbarAction?.showToolbar()
    }

    companion object {

        private const val REQUEST_CODE_VOICE_SEARCH = 2

        private const val REQUEST_OVERLAY_PERMISSION = 3

        /**
         * Animation's dutarion.
         */
        private const val ANIMATION_DURATION: Long = 50L

    }
}
