package jp.toastkid.yobidashi.browser

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.res.ColorStateList
import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.cleveroad.cyclemenuwidget.CycleMenuWidget
import com.cleveroad.cyclemenuwidget.OnMenuItemClickListener
import com.cleveroad.cyclemenuwidget.OnStateChangedListener
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.ArchivesActivity
import jp.toastkid.yobidashi.browser.bookmark.BookmarkActivity
import jp.toastkid.yobidashi.browser.history.ViewHistoryActivity
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.browser.webview.dialog.AnchorDialogCallback
import jp.toastkid.yobidashi.browser.webview.dialog.ImageDialogCallback
import jp.toastkid.yobidashi.databinding.FragmentBrowserBinding
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.editor.EditorModule
import jp.toastkid.yobidashi.libs.*
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.main.ToolbarAction
import jp.toastkid.yobidashi.pdf.PdfModule
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.search.SearchQueryExtractor
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingsActivity
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.tab_list.TabListModule
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection

/**
 * Internal browser fragment.
 *
 * @author toastkidjp
 */
class BrowserFragment : BaseFragment(),
        ImageDialogCallback,
        AnchorDialogCallback
{

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
     * Browser module.
     */
    private lateinit var browserModule: BrowserModule

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

    private lateinit var progressBarCallback: ProgressBarCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolbarAction = context as ToolbarAction?
        activity?.let {
            rxPermissions = RxPermissions(it)
        }
        activity?.let {
            if (it is ProgressBarCallback) {
                progressBarCallback = it
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_browser, container, false)
        binding?.fragment = this

        binding?.swipeRefresher?.let {
            it.setOnRefreshListener { tabs.reload() }
            it.setOnChildScrollUpCallback { _, _ -> browserModule.disablePullToRefresh() }
        }

        initMenus()

        val colorPair = colorPair()

        val activityContext = context ?: return null
        val cm = activityContext.applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        searchWithClip = SearchWithClip(
                cm,
                binding?.root as View,
                colorPair,
                { tabs.openNewWebTab() }
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
                binding?.moduleContainer as ViewGroup
        )

        browserModule = BrowserModule(
                context as Context,
                titleCallback = titleSubject::onNext,
                loadingCallback = { progress, loading ->
                    if (!loading) {
                        binding?.swipeRefresher?.isRefreshing = false
                        tabs.saveTabList()
                        val currentTab = tabs.currentTab()
                        tabs.deleteThumbnail(currentTab?.thumbnailPath)
                        tabs.saveNewThumbnailAsync()
                    }
                    progressSubject.onNext(progress)
                },
                historyAddingCallback = { title, url -> tabs.addHistory(title, url) },
                loader = { url, onBackground ->
                    if (onBackground) {
                        tabs.openBackgroundTab(url)
                    } else {
                        tabs.openNewWebTab(url)
                    }
                }
        )

        tabs = TabAdapter(
                binding?.webViewContainer as FrameLayout,
                browserModule,
                editor,
                pdf,
                titleSubject::onNext,
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
        tabs.openNewWebTab()
    }

    /**
     * Initialize menus view.
     */
    private fun initMenus() {
        binding?.cycleMenu?.also {
            it.setMenuItems(Menu.items(context))
            it.setOnMenuItemClickListener(object : OnMenuItemClickListener {
                override fun onMenuItemLongClick(view: View?, itemPosition: Int) {
                    Menu.showInformation(view)
                }

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
                hideHeader()
                true
            }
        }
    }

    /**
     * Hide footer with animation.
     */
    private fun hideHeader() {
        toolbarAction?.hideToolbar()
    }

    /**
     * Stop current tab's loading.
     */
    private fun stopCurrentLoading() {
        browserModule.stopLoading()
        Toaster.snackShort(binding?.root as View, R.string.message_stop_loading, colorPair())
    }

    /**
     * Menu action.
     *
     * @param id
     */
    private fun onMenuClick(id: Int) {
        val fragmentActivity = activity ?: return
        val snackbarParent = binding?.root as View
        when (id) {
            Menu.RELOAD.ordinal -> {
                tabs.reload()
            }
            Menu.BACK.ordinal -> {
                back()
            }
            Menu.FORWARD.ordinal -> {
                forward()
            }
            Menu.TOP.ordinal -> {
                toTop()
            }
            Menu.BOTTOM.ordinal -> {
                toBottom()
            }
            Menu.FIND_IN_PAGE.ordinal -> {
                if (pageSearcherModule?.isVisible == true) {
                    pageSearcherModule?.hide()
                    return
                }
                pageSearcherModule?.show(fragmentActivity)
            }
            Menu.SCREENSHOT.ordinal -> {
                browserModule.currentSnap()
                Toaster.snackShort(snackbarParent, R.string.message_done_save, colorPair())
            }
            Menu.SHARE.ordinal -> {
                startActivity(
                        IntentFactory.makeShare(browserModule.currentTitle()
                                + System.getProperty("line.separator") + browserModule.currentUrl())
                )
            }
            Menu.SETTING.ordinal -> {
                startActivity(SettingsActivity.makeIntent(fragmentActivity))
            }
            Menu.USER_AGENT.ordinal -> {
                UserAgent.showSelectionDialog(
                        snackbarParent,
                        { browserModule.resetUserAgent(it.text()) }
                )
            }
            Menu.WIFI_SETTING.ordinal -> {
                startActivity(SettingsIntentFactory.wifi())
            }
            Menu.PAGE_INFORMATION.ordinal -> {
                PageInformationDialogFragment()
                        .also { it.arguments = tabs.makeCurrentPageInformation() }
                        .show(
                                fragmentManager,
                                PageInformationDialogFragment::class.java.simpleName
                        )
            }
            Menu.TAB_LIST.ordinal -> {
                switchTabList()
            }
            Menu.STOP_LOADING.ordinal -> {
                stopCurrentLoading()
            }
            Menu.OTHER_BROWSER.ordinal -> {
                browserModule.currentUrl()?.let {
                    CustomTabsFactory.make(fragmentActivity, colorPair())
                            .build()
                            .launchUrl(fragmentActivity, Uri.parse(it))
                }
            }
            Menu.ARCHIVE.ordinal -> {
                browserModule.saveArchive()
            }
            Menu.SEARCH.ordinal -> {
                search(ActivityOptionsFactory.makeScaleUpBundle(binding?.cycleMenu as View))
            }
            Menu.SITE_SEARCH.ordinal -> {
                tabs.siteSearch()
            }
            Menu.VOICE_SEARCH.ordinal -> {
                try {
                    startActivityForResult(VoiceSearch.makeIntent(fragmentActivity), VoiceSearch.REQUEST_CODE)
                } catch (e: ActivityNotFoundException) {
                    Timber.e(e)
                    binding?.root?.let {
                        Toaster.snackShort(it, R.string.message_unabled_voice_search, colorPair())
                    }
                }
            }
            Menu.REPLACE_HOME.ordinal -> {
                browserModule.currentUrl()?.let {
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
            Menu.LOAD_HOME.ordinal -> {
                tabs.loadHome()
            }
            Menu.VIEW_HISTORY.ordinal -> {
                startActivityForResult(
                        ViewHistoryActivity.makeIntent(fragmentActivity),
                        ViewHistoryActivity.REQUEST_CODE
                )
            }
            Menu.BOOKMARK.ordinal -> {
                context?.let {
                    startActivityForResult(
                        BookmarkActivity.makeIntent(it),
                        BookmarkActivity.REQUEST_CODE
                    )
                }
            }
            Menu.ADD_BOOKMARK.ordinal -> {
                tabs.addBookmark {
                    bookmark(ActivityOptionsFactory.makeScaleUpBundle(binding?.cycleMenu as View))
                }
            }
            Menu.EDITOR.ordinal -> {
                openEditorTab()
            }
            Menu.PDF.ordinal -> {
                fragmentActivity.moveTaskToBack(true)
            }
            Menu.EXIT.ordinal -> {
                activity?.moveTaskToBack(true)
            }
        }
    }

    /**
     * Initialize tab list.
     */
    private fun initTabListIfNeed() {
        if (binding?.tabListContainer?.childCount == 0) {
            binding?.tabListContainer?.addView(tabListModule.moduleView)
        }
    }

    /**
     * Switch tab list visibility.
     */
    private fun switchTabList() {
        initTabListIfNeed()
        if (tabListModule.isVisible) {
            hideTabList()
        } else {
            tabs.updateCurrentTab()
            showTabList()
        }
    }

    /**
     * Close tab list module.
     */
    private fun closeTabList() {
        tabListModule.let { if (it.isVisible) { it.hide() } }
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

        titleSubject.subscribe({ progressBarCallback.onTitleChanged(it) }).addTo(disposables)
        progressSubject.subscribe({ progressBarCallback.onProgressChanged(it) }).addTo(disposables)

        tabs.loadBackgroundTabsFromDirIfNeed()

        if (pdf.isVisible) {
            pdf.applyColor(colorPair)
        }

        if (tabs.isNotEmpty()) {
            tabs.replaceToCurrentTab(false)
        } else {
            tabs.openNewWebTab(preferenceApplier().homeUrl)
        }

        val preferenceApplier = preferenceApplier()

        binding?.cycleMenu?.also {
            it.setCorner(preferenceApplier.menuPos())
            val color = preferenceApplier.colorPair().bgColor()
            it.setItemsBackgroundTint(ColorStateList.valueOf(color))
            context?.let { ContextCompat.getDrawable(it, R.drawable.ic_menu) }
                    ?.also { DrawableCompat.setTint(it, color) }
                    ?.let { drawable -> it.setCornerImageDrawable(drawable) }
        }

        browserModule.resizePool(preferenceApplier.poolSize)

        if (preferenceApplier.browserScreenMode() == ScreenMode.FULL_SCREEN
                || editor.isVisible
                || pdf.isVisible
                ) {
            hideHeader()
            return
        }

        binding?.swipeRefresher?.let {
            it.setProgressBackgroundColorSchemeColor(preferenceApplier.color)
            it.setColorSchemeColors(preferenceApplier.fontColor)
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

    override fun tapHeader() {
        val activityContext = context ?: return
        val currentUrl = browserModule.currentUrl()
        val inputText = if (preferenceApplier().enableSearchQueryExtract) {
            SearchQueryExtractor(currentUrl) ?: currentUrl
        } else {
            currentUrl
        }
        startActivity(SearchActivity.makeIntentWithQuery(activityContext, inputText ?: ""))
    }

    /**
     * Hide option menus.
     */
    private fun hideOption(): Boolean {

        if (tabListModule.isVisible) {
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
        tabs.replaceToCurrentTab(true)
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
                intent.data?.let {
                    tabs.openNewWebTab(it.toString())
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
        tabs.openNewWebTab(uri.toString())
    }

    override fun openNewTab(url: String) {
        tabs.openNewWebTab(url)
    }

    override fun openBackgroundTab(url: String) {
        tabs.openBackgroundTab(url)
    }

    override fun openCurrent(url: String) {
        tabs.callLoadUrl(url)
    }

    override fun onClickSetBackground(url: String) {
        val activityContext = context ?: return
        storeImage(url, activityContext).subscribe { file ->
            preferenceApplier().backgroundImagePath = file.absolutePath
            Toaster.snackShort(
                    binding?.root as View,
                    R.string.message_change_background_image,
                    preferenceApplier().colorPair()
            )
        }.addTo(disposables)
    }

    override fun onClickSaveForBackground(url: String) {
        val activityContext = context ?: return
        storeImage(url, activityContext).subscribe({
            Toaster.snackShort(
                    binding?.root as View,
                    R.string.message_done_save,
                    preferenceApplier().colorPair()
            )
        }).addTo(disposables)
    }

    override fun onClickDownloadImage(url: String) {
        ImageDownloadAction(binding?.root as View, url).invoke()
    }

    /**
     * Store image to file.
     *
     * @param url URL string.
     * @param context [Context]
     */
    private fun storeImage(url: String, context: Context): Maybe<File> {
        if (PreferenceApplier(context).wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return Maybe.empty()
        }
        return Single.fromCallable { HTTP_CLIENT.newCall(Request.Builder().url(url).build()).execute() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .filter { it.code() == HttpURLConnection.HTTP_OK }
                .map { BitmapFactory.decodeStream(it.body()?.byteStream()) }
                .map {
                    val storeroom = FilesDir(context, BackgroundSettingActivity.BACKGROUND_DIR)
                    val file = storeroom.assignNewFile(Uri.parse(url))
                    Bitmaps.compress(it, file)
                    file
                }
    }

    override fun onPause() {
        super.onPause()
        editor.saveIfNeed()
        binding?.tabListContainer?.removeAllViews()
        hideOption()
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
        toolbarAction?.showToolbar()
        browserModule.dispose()
    }

    companion object {

        /**
         * Request code of opening PDF.
         */
        private const val REQUEST_CODE_OPEN_PDF: Int = 3

        /**
         * HTTP Client.
         */
        private val HTTP_CLIENT by lazy { HttpClientFactory.make() }

    }

}
