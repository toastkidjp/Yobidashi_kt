package jp.toastkid.yobidashi.browser

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.about.AboutThisAppActivity
import jp.toastkid.yobidashi.barcode.BarcodeReaderActivity
import jp.toastkid.yobidashi.browser.archive.ArchivesActivity
import jp.toastkid.yobidashi.browser.bookmark.BookmarkActivity
import jp.toastkid.yobidashi.browser.floating.FloatingPreview
import jp.toastkid.yobidashi.browser.history.ViewHistoryActivity
import jp.toastkid.yobidashi.browser.menu.Menu
import jp.toastkid.yobidashi.browser.menu.MenuContract
import jp.toastkid.yobidashi.browser.menu.MenuPresenter
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.browser.user_agent.UserAgent
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.AnchorDialogCallback
import jp.toastkid.yobidashi.browser.webview.dialog.ImageDialogCallback
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.FragmentBrowserBinding
import jp.toastkid.yobidashi.databinding.ModuleEditorBinding
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.editor.*
import jp.toastkid.yobidashi.launcher.LauncherActivity
import jp.toastkid.yobidashi.libs.*
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.main.ToolbarAction
import jp.toastkid.yobidashi.pdf.PdfModule
import jp.toastkid.yobidashi.planning_poker.PlanningPokerActivity
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.search.SearchQueryExtractor
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingsActivity
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.tab_list.TabListClearDialogFragment
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment
import jp.toastkid.yobidashi.torch.Torch
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.math.abs

/**
 * Internal browser fragment.
 *
 * @author toastkidjp
 */
class BrowserFragment : BaseFragment(),
        ImageDialogCallback,
        AnchorDialogCallback,
        TabListClearDialogFragment.Callback,
        UserAgentDialogFragment.Callback,
        ClearTextDialogFragment.Callback,
        InputNameDialogFragment.Callback,
        PasteAsConfirmationDialogFragment.Callback,
        TabListDialogFragment.Callback,
        MenuContract.View
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
     * Progress bar callback.
     */
    private lateinit var progressBarCallback: ProgressBarCallback

    /**
     * Floating preview object.
     */
    private lateinit var floatingPreview: FloatingPreview

    /**
     * Torch API facade.
     */
    private lateinit var torch: Torch

    override lateinit var menuPresenter: MenuContract.Presenter

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
        torch = Torch(context)
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

        val colorPair = colorPair()

        val activityContext = context ?: return null
        val cm = activityContext.applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        searchWithClip = SearchWithClip(
                cm,
                binding?.root as View,
                colorPair
        ) { tabs.openNewWebTab() }
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
                scrollCallback = { _, vertical, _, old ->
                    val difference = vertical - old
                    if (abs(difference) < 15) {
                        return@BrowserModule
                    }
                    if (difference > 0) toolbarAction?.hideToolbar() else toolbarAction?.showToolbar()
                }
        )

        tabs = TabAdapter(
                binding?.webViewContainer as FrameLayout,
                browserModule,
                editorModule,
                pdfModule,
                titleSubject::onNext,
                this::onEmptyTabs
        )

        pageSearcherModule = PageSearcherModule(binding?.sip as ModuleSearcherBinding, tabs)

        menuPresenter = MenuPresenter(
                binding?.menusView,
                binding?.menuSwitch,
                this
        )

        setHasOptionsMenu(true)

        return binding?.root
    }

    /**
     * Action on empty tabs.
     */
    private fun onEmptyTabs() {
        tabListDialogFragment?.dismiss()
        tabs.openNewWebTab()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.browser, menu)

        menu?.let { menuNonNull ->
            menuNonNull.findItem(R.id.open_menu)?.setOnMenuItemClickListener {
                menuPresenter.switchMenuVisibility()
                true
            }

            menuNonNull.findItem(R.id.open_tabs)?.setOnMenuItemClickListener {
                switchTabList()
                true
            }

            val activityContext = context ?: return@let

            menuNonNull.findItem(R.id.setting)?.setOnMenuItemClickListener {
                startActivity(SettingsActivity.makeIntent(activityContext))
                true
            }

            menuNonNull.findItem(R.id.stop_loading)?.setOnMenuItemClickListener {
                stopCurrentLoading()
                true
            }

            menuNonNull.findItem(R.id.close_header)?.setOnMenuItemClickListener {
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
     * @param menu [Menu]
     */
    override fun onMenuClick(menu: Menu) {
        val fragmentActivity = activity ?: return
        val snackbarParent = binding?.root as View
        when (menu) {
            Menu.RELOAD -> {
                tabs.reload()
            }
            Menu.BACK-> {
                back()
            }
            Menu.FORWARD-> {
                forward()
            }
            Menu.TOP-> {
                toTop()
            }
            Menu.BOTTOM-> {
                toBottom()
            }
            Menu.FIND_IN_PAGE-> {
                if (pageSearcherModule?.isVisible == true) {
                    pageSearcherModule?.hide()
                    return
                }
                pageSearcherModule?.show(fragmentActivity)
            }
            Menu.SCREENSHOT-> {
                browserModule.currentSnap()
                Toaster.snackShort(snackbarParent, R.string.message_done_save, colorPair())
            }
            Menu.SHARE-> {
                startActivity(
                        IntentFactory.makeShare(browserModule.currentTitle()
                                + System.getProperty("line.separator") + browserModule.currentUrl())
                )
            }
            Menu.SETTING-> {
                startActivity(SettingsActivity.makeIntent(fragmentActivity))
            }
            Menu.USER_AGENT-> {
                val dialogFragment = UserAgentDialogFragment()
                dialogFragment.setTargetFragment(this, 1)
                dialogFragment.show(
                        fragmentManager,
                        UserAgentDialogFragment::class.java.simpleName
                )
            }
            Menu.WIFI_SETTING-> {
                startActivity(SettingsIntentFactory.wifi())
            }
            Menu.PAGE_INFORMATION-> {
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
            Menu.OTHER_BROWSER-> {
                browserModule.currentUrl()?.let {
                    CustomTabsFactory.make(fragmentActivity, colorPair())
                            .build()
                            .launchUrl(fragmentActivity, Uri.parse(it))
                }
            }
            Menu.ARCHIVE-> {
                browserModule.saveArchive()
            }
            Menu.SEARCH-> {
                search(ActivityOptionsFactory.makeScaleUpBundle(binding?.menusView as View))
            }
            Menu.SITE_SEARCH-> {
                tabs.siteSearch()
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
                    preferenceApplier().homeUrl = it
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
                    bookmark(ActivityOptionsFactory.makeScaleUpBundle(binding?.menusView as View))
                }
            }
            Menu.EDITOR-> {
                openEditorTab()
            }
            Menu.PDF-> {
                openPdfTabFromStorage()
            }
            Menu.CODE_READER -> {
                startActivity(BarcodeReaderActivity.makeIntent(fragmentActivity))
            }
            Menu.SCHEDULE-> {
                try {
                    startActivity(IntentFactory.makeCalendar())
                } catch (e: ActivityNotFoundException) {
                    Timber.w(e)
                }
            }
            Menu.OVERLAY_COLOR_FILTER-> {
                val activity = activity
                val rootView = binding?.root
                if (activity == null || rootView == null) {
                    return
                }
                ColorFilter(activity, rootView).switchState(activity)
            }
            Menu.PLANNING_POKER-> {
                context?.let { startActivity(PlanningPokerActivity.makeIntent(it)) }
            }
            Menu.CAMERA-> {
                useCameraPermission { startActivity(IntentFactory.camera()) }
            }
            Menu.TORCH-> {
                useCameraPermission { torch.switch() }
            }
            Menu.APP_LAUNCHER-> {
                context?.let { startActivity(LauncherActivity.makeIntent(it)) }
            }
            Menu.ABOUT-> {
                context?.let { startActivity(AboutThisAppActivity.makeIntent(it)) }
            }
            Menu.EXIT-> {
                activity?.moveTaskToBack(true)
            }
        }
    }

    override fun getTabCount() = tabs.size()

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
            val query = SearchQueryExtractor.invoke(browserModule.currentUrl())
            val makeIntent = if (TextUtils.isEmpty(query) || Urls.isValidUrl(query)) {
                SearchActivity.makeIntent(it)
            } else {
                SearchActivity.makeIntentWithQuery(it, query ?: "")
            }
            startActivity(makeIntent, option.toBundle())
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
        editorModule.applySettings()

        ClippingUrlOpener(binding?.root) { loadWithNewTab(it) }

        titleSubject.subscribe(
                { progressBarCallback.onTitleChanged(it) },
                Timber::e
        ).addTo(disposables)

        progressSubject.subscribe(
                { progressBarCallback.onProgressChanged(it) },
                Timber::e
        ).addTo(disposables)

        tabs.loadBackgroundTabsFromDirIfNeed()

        if (pdfModule.isVisible) {
            pdfModule.applyColor(colorPair)
        }

        if (tabs.isNotEmpty()) {
            tabs.replaceToCurrentTab(false)
        } else {
            tabs.openNewWebTab(preferenceApplier().homeUrl)
        }

        val preferenceApplier = preferenceApplier()

        menuPresenter.onResume { editorModule.setSpace(it) }

        browserModule.resizePool(preferenceApplier.poolSize)

        binding?.swipeRefresher?.let {
            it.setProgressBackgroundColorSchemeColor(preferenceApplier.color)
            it.setColorSchemeColors(preferenceApplier.fontColor)
        }

        val browserScreenMode = preferenceApplier.browserScreenMode()
        if (browserScreenMode == ScreenMode.FULL_SCREEN
                || editorModule.isVisible
                || pdfModule.isVisible
        ) {
            hideHeader()
            return
        }
        if (browserScreenMode == ScreenMode.EXPANDABLE
            || browserScreenMode == ScreenMode.FIXED) {
            toolbarAction?.showToolbar()
            return
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

        if (tabListDialogFragment?.isVisible == true) {
            hideTabList()
            return true
        }

        if (menuPresenter.isVisible()) {
            menuPresenter.close()
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
    }

    /**
     * Show tab list.
     */
    private fun showTabList() {
        tabListDialogFragment?.show(fragmentManager, "")
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
                val uri = intent.data ?: return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    val takeFlags: Int = intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context?.contentResolver?.takePersistableUriPermission(uri, takeFlags)
                }
                tabs.openNewPdfTab(uri)
            }
            BookmarkActivity.REQUEST_CODE, ViewHistoryActivity.REQUEST_CODE -> {
                intent.data?.let {
                    tabs.openNewWebTab(it.toString())
                }
            }
            EditorModule.REQUEST_CODE_LOAD -> {
                intent.data?.let { editorModule.readFromFileUri(it) }
            }
        }
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

    override fun openNewTab(url: String) {
        tabs.openNewWebTab(url)
    }

    override fun openBackgroundTab(url: String) {
        tabs.openBackgroundTab(url)
    }

    override fun openCurrent(url: String) {
        tabs.callLoadUrl(url)
    }

    override fun preview(url: String) {
        val webView = browserModule.getWebView("preview")
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

        binding?.floatingPreview?.let {
            floatingPreview = FloatingPreview(it)
            floatingPreview.invoke(webView, url)
        }
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
        storeImage(url, activityContext).subscribe {
            Toaster.snackShort(
                    binding?.root as View,
                    R.string.message_done_save,
                    preferenceApplier().colorPair()
            )
        }.addTo(disposables)
    }

    override fun onClickDownloadImage(url: String) {
        val activityContext = context ?: return
        if (Urls.isInvalidUrl(url)) {
            Toaster.snackShort(
                    binding?.root as View,
                    activityContext.getString(R.string.message_cannot_downloading_image),
                    PreferenceApplier(activityContext).colorPair()
            )
            return
        }

        ImageDownloadActionDialogFragment.show(activityContext, url)
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

    override fun onClickClear() {
        tabs.clear()
        onEmptyTabs()
    }

    override fun onClickUserAgent(userAgent: UserAgent) {
        browserModule.resetUserAgent(userAgent.text())
        Toaster.snackShort(
                binding?.root as View,
                getString(R.string.format_result_user_agent, userAgent.title()),
                preferenceApplier().colorPair()
        )
    }

    override fun onClickClearInput() {
        editorModule.clearInput()
    }

    override fun onClickInputName(fileName: String) {
        editorModule.assignNewFile(fileName)
    }

    override fun onClickPasteAs() {
        val activityContext = context ?: return
        val primary = Clipboard.getPrimary(activityContext)
        if (TextUtils.isEmpty(primary)) {
            return
        }
        editorModule.insert(Quotation(primary))
    }

    override fun onCloseTabListDialogFragment() = hideTabList()

    override fun onOpenEditor() = openEditorTab()

    override fun onOpenPdf() = openPdfTabFromStorage()

    override fun openNewTabFromTabList() = tabs.openNewWebTab()

    override fun tabIndexFromTabList() = tabs.index()

    override fun currentTabIdFromTabList() = tabs.currentTabId()

    override fun replaceTabFromTabList(tab: Tab) = tabs.replace(tab)

    override fun getTabByIndexFromTabList(position: Int): Tab = tabs.getTabByIndex(position)

    override fun closeTabFromTabList(position: Int) = tabs.closeTab(position)

    override fun getTabAdapterSizeFromTabList(): Int = tabs.size()

    override fun swapTabsFromTabList(from: Int, to: Int) = tabs.swap(from, to)

    override fun tabIndexOfFromTabList(tab: Tab): Int = tabs.indexOf(tab)

    override fun onPause() {
        super.onPause()
        editorModule.saveIfNeed()
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

    override fun onDetach() {
        super.onDetach()
        torch.dispose()
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
