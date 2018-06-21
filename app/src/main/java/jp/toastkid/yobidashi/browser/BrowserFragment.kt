package jp.toastkid.yobidashi.browser

import android.Manifest
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
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import com.cleveroad.cyclemenuwidget.CycleMenuWidget
import com.cleveroad.cyclemenuwidget.OnMenuItemClickListener
import com.cleveroad.cyclemenuwidget.OnStateChangedListener
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.ArchivesActivity
import jp.toastkid.yobidashi.browser.bookmark.BookmarkActivity
import jp.toastkid.yobidashi.browser.history.ViewHistoryActivity
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.databinding.FragmentBrowserBinding
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.editor.EditorModule
import jp.toastkid.yobidashi.libs.ActivityOptionsFactory
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.main.ToolbarAction
import jp.toastkid.yobidashi.pdf.PdfModule
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingsActivity
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.history.TabHistoryActivity
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.WebTab
import jp.toastkid.yobidashi.tab.tab_list.TabListModule
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
     * RxPermissions.
     */
    private var rxPermissions: RxPermissions? = null

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
     * Editor module.
     */
    private lateinit var editor: EditorModule

    /**
     * PDF module.
     */
    private lateinit var pdf: PdfModule

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
     * PublishSubject of title pair.
     */
    private val titleSubject: PublishSubject<TitlePair> = PublishSubject.create<TitlePair>()

    /**
     * PublishSubject of title pair.
     */
    private val progressSubject: PublishSubject<Int> = PublishSubject.create<Int>()

    /**
     * Composite disposer.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * This value is assigned by OnStateChangeListener.
     */
    private var menuOpen: Boolean = false

    /**
     * Set consumer to titleSubject.
     */
    var consumer: Consumer<TitlePair>? = null

    /**
     * Set consumer to titleSubject.
     */
    var progressConsumer: Consumer<Int>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolbarAction = context as ToolbarAction?
        activity?.let {
            rxPermissions = RxPermissions(it)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_browser, container, false)
        binding?.fragment = this

        binding?.webViewContainer?.let {
            it.setOnRefreshListener { tabs.reload() }
            it.setOnChildScrollUpCallback { _, _ -> tabs.disablePullToRefresh() }
        }

        initMenus()

        val colorPair = colorPair()

        val activityContext = context ?: return null
        val cm = activityContext.applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        searchWithClip = SearchWithClip(
                cm,
                binding?.root as View,
                colorPair,
                { tabs.loadWithNewTab(Uri.parse(it)) }
        )
        searchWithClip.invoke()

        editor = EditorModule(
                binding?.editor as ModuleEditorBinding,
                { intent, requestCode -> startActivityForResult(intent, requestCode) },
                this::switchTabList,
                this::closeTabList,
                { file ->
                    val currentTab = tabs.currentTab()
                    if (currentTab is EditorTab) {
                        currentTab.setFileInformation(file)
                        tabs.saveTabList()
                    }
                },
                this::hideOption
        )

        pdf = PdfModule(
                activityContext,
                binding?.moduleContainer as ViewGroup,
                { /* TODO remove */ }
        )

        tabs = TabAdapter(
                binding?.webViewContainer as ViewGroup,
                editor,
                pdf,
                titleSubject::onNext,
                { progress, loading ->
                    if (!loading) { binding?.webViewContainer?.isRefreshing = false}
                    progressSubject.onNext(progress)
                },
                this::hideOption,
                this::onEmptyTabs
        )

        tabListModule = TabListModule(
                DataBindingUtil.inflate(
                        LayoutInflater.from(activity), R.layout.module_tab_list, null, false),
                tabs,
                binding?.root as View,
                this::hideTabList,
                this::openEditorTab,
                this::openPdfTabFromStorage,
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
        fragmentManager?.popBackStack()
    }

    /**
     * Initialize menus view.
     */
    private fun initMenus() {
        binding?.cycleMenu?.also {
            it.setMenuRes(R.menu.browser_menu)
            it.setOnMenuItemClickListener(object : OnMenuItemClickListener {
                override fun onMenuItemLongClick(view: View?, itemPosition: Int) = Unit

                override fun onMenuItemClick(view: View?, itemPosition: Int) {
                    onMenuClick(view?.id ?: 0)
                }
            })
            it.setStateChangeListener(object : OnStateChangedListener {
                override fun onCloseComplete() = Unit

                override fun onOpenComplete() = Unit

                override fun onStateChanged(state: CycleMenuWidget.STATE?) {
                    when (state) {
                        CycleMenuWidget.STATE.OPEN -> menuOpen = true
                        CycleMenuWidget.STATE.CLOSED -> menuOpen = false
                        else -> Unit
                    }
                }
            })
        }
    }

    /**
     * Switch menu visibility.
     */
    private fun switchMenu() {
        hideTabList()
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

            val activityContext = context ?: return@let

            it.findItem(R.id.setting)?.setOnMenuItemClickListener {
                startActivity(SettingsActivity.makeIntent(activityContext))
                true
            }

            it.findItem(R.id.stop_loading)?.setOnMenuItemClickListener {
                stopCurrentLoading()
                true
            }

            it.findItem(R.id.close_header)?.setOnMenuItemClickListener {
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
    private fun onMenuClick(id: Int) {
        val fragmentActivity = activity ?: return
        val snackbarParent = binding?.root as View
        when (id) {
            R.id.reload -> {
                tabs.reload()
            }
            R.id.back -> {
                back()
            }
            R.id.forward -> {
                forward()
            }
            R.id.to_top -> {
                toTop()
            }
            R.id.to_bottom -> {
                toBottom()
            }
            R.id.find_in_page -> {
                if (pageSearcherModule?.isVisible ?: false) {
                    pageSearcherModule?.hide()
                    return
                }
                pageSearcherModule?.show(fragmentActivity)
            }
            R.id.screenshot -> {
                tabs.currentSnap()
                Toaster.snackShort(snackbarParent, R.string.message_done_save, colorPair())
            }
            R.id.share -> {
                startActivity(
                        IntentFactory.makeShare(tabs.currentTitle()
                                + System.getProperty("line.separator") + tabs.currentUrl())
                )
            }
            R.id.setting -> {
                startActivity(SettingsActivity.makeIntent(fragmentActivity))
            }
            R.id.tab_history -> {
                launchTabHistory(fragmentActivity)
            }
            R.id.user_agent -> {
                UserAgent.showSelectionDialog(
                        snackbarParent,
                        { tabs.resetUserAgent(it.text()) }
                )
            }
            R.id.wifi -> {
                startActivity(SettingsIntentFactory.wifi())
            }
            R.id.page_information -> {
                tabs.showPageInformation()
            }
            R.id.tab_list -> {
                switchTabList()
            }
            R.id.stop_loading -> {
                stopCurrentLoading()
            }
            R.id.open -> {
                val inputLayout = TextInputs.make(fragmentActivity)
                inputLayout.editText?.setText(tabs.currentUrl())
                AlertDialog.Builder(fragmentActivity)
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
            R.id.open_other -> {
                tabs.currentUrl()?.let {
                    CustomTabsFactory.make(fragmentActivity, colorPair())
                            .build()
                            .launchUrl(fragmentActivity, Uri.parse(it))
                }
            }
            R.id.share_by_code -> {
                SharingUrlByBarcode.invoke(fragmentActivity, tabs.currentUrl() ?: "")
            }
            R.id.archive -> {
                tabs.saveArchive()
            }
            R.id.search -> {
                search(ActivityOptionsFactory.makeScaleUpBundle(binding?.cycleMenu as View))
            }
            R.id.site_search -> {
                tabs.siteSearch()
            }
            R.id.voice_search -> {
                startActivityForResult(VoiceSearch.makeIntent(fragmentActivity), VoiceSearch.REQUEST_CODE)
            }
            R.id.replace_home -> {
                tabs.currentUrl()?.let {
                    if (Urls.isInvalidUrl(it)) {
                        Toaster.snackShort(
                                snackbarParent,
                                R.string.message_cannot_replace_home_url,
                                colorPair()
                        )
                        return
                    }
                    preferenceApplier().homeUrl = it
                    Toaster.snackShort(
                            snackbarParent,
                            getString(R.string.message_replace_home_url, it) ,
                            colorPair()
                    )
                }
            }
            R.id.load_home -> {
                tabs.loadHome()
            }
            R.id.view_history -> {
                startActivityForResult(
                        ViewHistoryActivity.makeIntent(fragmentActivity),
                        ViewHistoryActivity.REQUEST_CODE
                )
            }
            R.id.add_bookmark -> {
                tabs.addBookmark {
                    bookmark(ActivityOptionsFactory.makeScaleUpBundle(binding?.cycleMenu as View))
                }
            }
            R.id.editor -> {
                openEditorTab()
            }
            R.id.pdf -> {
                fragmentActivity.moveTaskToBack(true)
            }
            R.id.exit -> {
                activity?.moveTaskToBack(true)
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
                binding?.cycleMenu, 0, 0,
                binding?.cycleMenu?.width ?: 0, binding?.cycleMenu?.height ?: 0)
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
     * @param ignored Snackbar's parent view.
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
            tabs.updateCurrentTab()
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
            startActivity(SearchActivity.makeIntent(it), option.toBundle())
        }
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

        val colorPair = colorPair()
        editor.applyColor()

        consumer?.let { titleSubject.subscribe(it).addTo(disposables) }

        tabs.reloadWebViewSettings().addTo(disposables)
        progressConsumer?.also { progressSubject.subscribe(it).addTo(disposables) }

        tabs.loadBackgroundTabsFromDirIfNeed()

        if (pdf.isVisible) {
            pdf.applyColor(colorPair)
        }

        if (tabs.isNotEmpty()) {
            tabs.setCurrentTab()
            tabs.replaceToCurrentTab(false)
        } else {
            tabs.loadWithNewTab(Uri.parse(preferenceApplier().homeUrl))
        }

        val preferenceApplier = preferenceApplier()
        if (preferenceApplier.browserScreenMode() == ScreenMode.FULL_SCREEN
                || editor.isVisible
                || pdf.isVisible
                ) {
            return
        }

        binding?.webViewContainer?.let {
            it.setProgressBackgroundColorSchemeColor(preferenceApplier.color)
            it.setColorSchemeColors(preferenceApplier.fontColor)
        }

    }

    override fun pressLongBack(): Boolean {
        activity?.let { launchTabHistory(it) }
        return true
    }

    override fun pressBack(): Boolean = hideOption() || back()

    override fun tapHeader() {
        val activityContext = context
        if (activityContext == null) {
            return
        }
        startActivity(SearchActivity.makeIntentWithQuery(activityContext, tabs.currentUrl() ?: ""))
    }

    /**
     * Hide option menus.
     */
    private fun hideOption(): Boolean {

        if (tabListModule != null && tabListModule.isVisible as Boolean) {
            hideTabList()
            return true
        }

        if (menuOpen) {
            binding?.cycleMenu?.close(true)
            return true
        }

        return false
    }

    /**
     * Hide tab list.
     */
    private fun hideTabList() {
        tabListModule.hide()
    }

    /**
     * Show tab list.
     */
    private fun showTabList() {
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
            }
            VoiceSearch.REQUEST_CODE -> {
                activity?.let {
                    VoiceSearch.processResult(it, intent).addTo(disposables)
                }
            }
            REQUEST_CODE_OPEN_PDF -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    val takeFlags: Int = intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context?.contentResolver?.takePersistableUriPermission(intent.data, takeFlags)
                }
                tabs.openNewPdfTab(intent.data)
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

    /**
     * Open PDF from storage.
     */
    private fun openPdfTabFromStorage() {
        rxPermissions
                ?.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                ?.subscribe { granted ->
                    if (!granted) {
                        return@subscribe
                    }
                    startActivityForResult(
                            IntentFactory.makeOpenDocument("application/pdf"),
                            REQUEST_CODE_OPEN_PDF
                    )
                }
    }

    /**
     * Open Editor tab.
     */
    private fun openEditorTab() {
        rxPermissions
                ?.request(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
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
        hideOption()
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
        toolbarAction?.showToolbar()
    }

    companion object {

        /**
         * Request code of opening PDF.
         */
        private const val REQUEST_CODE_OPEN_PDF: Int = 3

    }

}
