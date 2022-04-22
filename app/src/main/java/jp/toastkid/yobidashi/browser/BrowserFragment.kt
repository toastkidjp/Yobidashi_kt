package jp.toastkid.yobidashi.browser

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.tab.OnBackCloseableTabUiFragment
import jp.toastkid.lib.viewmodel.PageSearcherViewModel
import jp.toastkid.rss.extractor.RssUrlFinder
import jp.toastkid.search.SearchQueryExtractor
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.history.ViewHistoryFragment
import jp.toastkid.yobidashi.browser.page_information.PageInformationDialogFragment
import jp.toastkid.yobidashi.browser.reader.ReaderFragment
import jp.toastkid.yobidashi.browser.reader.ReaderFragmentViewModel
import jp.toastkid.yobidashi.browser.shortcut.ShortcutUseCase
import jp.toastkid.yobidashi.browser.translate.TranslatedPageOpenerUseCase
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDialogFragment
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDropdown
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.search.SearchFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Internal browser fragment.
 *
 * @author toastkidjp
 */
class BrowserFragment : Fragment(),
    OnBackCloseableTabUiFragment,
    CommonFragmentAction,
    ContentScrollable
{

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * Browser module.
     */
    private lateinit var browserModule: BrowserModule

    private val searchQueryExtractor = SearchQueryExtractor()

    private var appBarViewModel: AppBarViewModel? = null

    private var contentViewModel: ContentViewModel? = null

    private var tabListViewModel: TabListViewModel? = null

    private val storagePermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                val context = context ?: return@registerForActivityResult
                Toaster.tShort(context, R.string.message_requires_permission_storage)
                return@registerForActivityResult
            }

            browserModule.downloadAllImages()
        }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
/*TODO swipe refresher
        binding?.swipeRefresher?.let {
            it.setOnRefreshListener { reload() }
            it.setOnChildScrollUpCallback { _, _ -> browserModule.disablePullToRefresh() }
            it.setDistanceToTriggerSync(500)
        }
*/

        val activityContext = context ?: return null

        preferenceApplier = PreferenceApplier(activityContext)

        val webViewContainer = FrameLayout(activityContext)
        browserModule = BrowserModule(activityContext, webViewContainer)

        setHasOptionsMenu(true)

        return ComposeViewFactory().invoke(activityContext) {
            MaterialTheme {
                AndroidView(
                    factory = {
                        webViewContainer
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity ?: return

        initializeHeaderViewModels(activity)

        contentViewModel = ViewModelProvider(activity).get(ContentViewModel::class.java)
    }

    private fun initializeHeaderViewModels(activity: FragmentActivity) {
        val viewModelProvider = ViewModelProvider(activity)
        appBarViewModel = viewModelProvider.get(AppBarViewModel::class.java)

        viewModelProvider.get(BrowserHeaderViewModel::class.java).also { viewModel ->
            viewModel.stopProgress.observe(viewLifecycleOwner, Observer {
                val stop = it?.getContentIfNotHandled() ?: return@Observer
                if (stop.not()
                    //TODO || binding?.swipeRefresher?.isRefreshing == false
                ) {
                    return@Observer
                }
                stopSwipeRefresherLoading()
            })
        }

        tabListViewModel = viewModelProvider.get(TabListViewModel::class.java)

        viewModelProvider.get(BrowserHeaderViewModel::class.java).also { viewModel ->
            viewModel.reset.observe(viewLifecycleOwner, Observer {
                if (!isVisible) {
                    return@Observer
                }

                val context = context ?: return@Observer

                appBarViewModel?.replace(context) {
                    val tint = Color(preferenceApplier.fontColor)

                    val headerTitle = viewModel.title.observeAsState()
                    val headerUrl = viewModel.url.observeAsState()
                    val progress = viewModel.progress.observeAsState()
                    val enableBack = viewModel.enableBack.observeAsState()
                    val enableForward = viewModel.enableForward.observeAsState()
                    val tabCountState = tabListViewModel?.tabCount?.observeAsState()

                    Column(modifier = Modifier
                        .height(76.dp)
                        .fillMaxWidth()
                    ) {
                        if (progress.value ?: 0 < 70) {
                            LinearProgressIndicator(
                                progress = (progress.value?.toFloat() ?: 100f) / 100f,
                                color = Color(preferenceApplier.fontColor),
                                modifier = Modifier.height(1.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HeaderSubButton(
                                R.drawable.ic_back,
                                R.string.back,
                                tint,
                                enableBack.value ?: false
                            ) { back() }
                            HeaderSubButton(
                                R.drawable.ic_forward,
                                R.string.title_menu_forward,
                                tint,
                                enableForward.value ?: false
                            ) { forward() }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .padding(8.dp)
                                    .clickable { tabList() }
                            ) {
                                Image(
                                    painterResource(R.drawable.ic_tab),
                                    contentDescription = stringResource(id = R.string.tab_list),
                                    colorFilter = ColorFilter.tint(Color(preferenceApplier.fontColor), BlendMode.SrcIn),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                )
                                Text(
                                    text = "${tabCountState?.value ?: 0}",
                                    color = Color(preferenceApplier.fontColor),
                                    fontSize = 10.sp
                                )
                            }
                            Box {
                                val open = remember { mutableStateOf(false) }
                                HeaderSubButton(
                                    R.drawable.ic_user_agent,
                                    R.string.title_user_agent,
                                    tint
                                ) { open.value = true }
                                UserAgentDropdown(open) {
                                    preferenceApplier.setUserAgent(it.name)
                                    browserModule.resetUserAgent(it.text())
                                    contentViewModel?.snackShort(
                                        activity.getString(
                                            R.string.format_result_user_agent,
                                            it.title()
                                        )
                                    )
                                }
                            }
                            HeaderSubButton(
                                R.drawable.ic_info,
                                R.string.title_menu_page_information,
                                tint
                            ) { showPageInformation() }
                            HeaderSubButton(
                                R.drawable.ic_code,
                                R.string.title_menu_html_source,
                                tint
                            ) { showHtmlSource() }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(32.dp)
                                .fillMaxWidth()
                                .clickable { tapHeader() }
                        //url_box_background
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_reader_mode),
                                contentDescription = stringResource(id = R.string.title_menu_reader_mode),
                                tint = tint,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { openReaderMode() }
                            )
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                val progressTitle =
                                    if (progress.value ?: 100 < 70)
                                        activity.getString(R.string.prefix_loading) + "${progress.value}%"
                                    else
                                        headerTitle.value ?: ""

                                Text(
                                    text = progressTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = tint,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = headerUrl.value ?: "",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = tint,
                                    fontSize = 10.sp
                                )
                            }

                            val isNotLoading = 70 < progress.value ?: 100
                            val reloadIconId = if (isNotLoading) R.drawable.ic_reload else R.drawable.ic_close
                            Icon(
                                painterResource(id = reloadIconId),
                                contentDescription = stringResource(id = R.string.title_menu_reload),
                                tint = tint,
                                modifier = Modifier
                                    .clickable {
                                        if (isNotLoading) {
                                            browserModule.reload()
                                        } else {
                                            browserModule.stopLoading()
                                            stopSwipeRefresherLoading()
                                        }
                                    }
                            )
                        }
                    }
                }
            })
        }

        CoroutineScope(Dispatchers.Main).launch {
            viewModelProvider.get(LoadingViewModel::class.java)
                    .onPageFinished
                    .collect {
                        browserModule.saveArchiveForAutoArchive()
                    }
        }

        viewModelProvider.get(BrowserFragmentViewModel::class.java)
                .loadWithNewTab
                .observe(activity, {
                    browserModule.loadWithNewTab(it.first, it.second)
                })

        viewModelProvider.get(PageSearcherViewModel::class.java).also { viewModel ->
            viewModel.find.observe(viewLifecycleOwner, Observer {
                browserModule.find(it)
            })

            viewModel.upward.observe(viewLifecycleOwner, Observer {
                browserModule.findUp()
            })

            viewModel.downward.observe(viewLifecycleOwner, Observer {
                browserModule.findDown()
            })
        }
    }

    @Composable
    private fun HeaderSubButton(
        iconId: Int,
        descriptionId: Int,
        tint: Color,
        enable: Boolean = true,
        onClick: () -> Unit
    ) {
        Icon(
            painterResource(id = iconId),
            contentDescription = stringResource(id = descriptionId),
            tint = tint,
            modifier = Modifier
                .width(40.dp)
                .padding(4.dp)
                .alpha(if (enable) 1f else 0.6f)
                .clickable(enabled = enable, onClick = onClick)
        )
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.browser, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.translate -> {
                val activity = activity ?: return true
                val browserViewModel =
                        ViewModelProvider(activity).get(BrowserViewModel::class.java)
                TranslatedPageOpenerUseCase(browserViewModel).invoke(browserModule.currentUrl())
            }
            R.id.download_all_images -> {
                storagePermissionRequestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            R.id.add_to_home -> {
                val uri = browserModule.currentUrl()?.toUri() ?: return true
                val context = context ?: return true
                ShortcutUseCase(context)
                        .invoke(
                                uri,
                                browserModule.currentTitle(),
                                FaviconApplier(context).load(uri)
                        )
            }
            R.id.add_bookmark -> {
                val context = context ?: return true
                val faviconApplier = FaviconApplier(context)
                val url = browserModule.currentUrl() ?: ""
                BookmarkInsertion(
                        context,
                        browserModule.currentTitle(),
                        url,
                        faviconApplier.makePath(url),
                        Bookmark.getRootFolderName()
                ).insert()

                contentViewModel?.snackShort(context.getString(R.string.message_done_added_bookmark))
                return true
            }
            R.id.add_archive -> {
                browserModule.saveArchive()
                return true
            }
            R.id.add_rss -> {
                RssUrlFinder(preferenceApplier).invoke(browserModule.currentUrl()) { null /* TODO*/ }
                return true
            }
            R.id.replace_home -> {
                browserModule.currentUrl()?.let {
                    val context = context ?: return true
                    if (Urls.isInvalidUrl(it)) {
                        contentViewModel?.snackShort(context.getString(R.string.message_cannot_replace_home_url))
                        return true
                    }
                    preferenceApplier.homeUrl = it
                    contentViewModel?.snackShort(context.getString(R.string.message_replace_home_url, it))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun reload() {
        // TODO Delete it
    }

    fun openReaderMode() {
        browserModule.invokeContentExtraction(ValueCallback(this::showReaderFragment))
    }

    fun tabList() {
        contentViewModel?.switchTabList()
    }

    fun openNewTab(): Boolean {
        tabListViewModel?.openNewTab()
        return true
    }

    private fun showReaderFragment(content: String) {
        val cleaned = content.replace("^\"|\"$".toRegex(), "")
        if (cleaned.isBlank()) {
            contentViewModel?.snackShort("This page can't show reader mode.")
            return
        }

        val readerFragment =
                activity?.supportFragmentManager?.findFragmentByTag(ReaderFragment::class.java.canonicalName)
                        ?: ReaderFragment()

        val lineSeparator = System.lineSeparator()
        val replacedContent = cleaned.replace("\\n", lineSeparator)

        activity?.let {
            ViewModelProvider(it).get(ReaderFragmentViewModel::class.java)
                    .setContent(browserModule.currentTitle(), replacedContent)
        }

        contentViewModel?.nextFragment(readerFragment)
    }

    fun showUserAgentSetting() {
        val dialogFragment = UserAgentDialogFragment()
        dialogFragment.show(
                parentFragmentManager,
                UserAgentDialogFragment::class.java.simpleName
        )
    }

    fun showPageInformation() {
        val pageInformation = browserModule.makeCurrentPageInformation()
        if (pageInformation.isEmpty) {
            return
        }

        PageInformationDialogFragment()
                .also { it.arguments = pageInformation }
                .show(
                        parentFragmentManager,
                        PageInformationDialogFragment::class.java.simpleName
                )
    }

    fun showHtmlSource() {
        browserModule.invokeHtmlSourceExtraction {
            showReaderFragment(it.replace("\\u003C", "<"))
        }
    }

    /**
     * Do browser back action.
     */
    fun back(): Boolean = browserModule.back()

    /**
     * Do browser forward action.
     */
    fun forward() = browserModule.forward()

    fun showHistory(): Boolean {
        contentViewModel?.nextFragment(ViewHistoryFragment::class.java)
        return true
    }

    fun getTitleAndUrl(): Pair<String?, String?> {
        val currentTitle = browserModule.currentTitle()
        val currentUrl = browserModule.currentUrl()
        return currentTitle to currentUrl
    }

    /**
     * Go to bottom.
     */
    override fun toBottom() {
        browserModule.pageDown()
    }

    /**
     * Go to top.
     */
    override fun toTop() {
        browserModule.pageUp()
    }

    override fun onResume() {
        super.onResume()

        switchToolbarVisibility()

        browserModule.resizePool(preferenceApplier.poolSize)
        browserModule.applyNewAlpha()
        browserModule.onResume()
/*TODO
        binding?.swipeRefresher?.let {
            it.setProgressBackgroundColorSchemeColor(preferenceApplier.color)
            it.setColorSchemeColors(preferenceApplier.fontColor)
        }*/
    }

    private fun switchToolbarVisibility() {
        when (ScreenMode.find(preferenceApplier.browserScreenMode())) {
            ScreenMode.FULL_SCREEN -> appBarViewModel?.hide()
            ScreenMode.EXPANDABLE, ScreenMode.FIXED -> appBarViewModel?.show()
        }
    }

    override fun pressBack(): Boolean = back()

    override fun share() {
        startActivity(
            ShareIntentFactory()(browserModule.makeShareMessage())
        )
    }

    fun tapHeader() {
        val currentTitle = browserModule.currentTitle()
        val currentUrl = browserModule.currentUrl()
        val inputText = if (preferenceApplier.enableSearchQueryExtract) {
            searchQueryExtractor(currentUrl) ?: currentUrl
        } else {
            currentUrl
        }

        val fragment = SearchFragment.makeWithQuery(
                inputText ?: "",
                currentTitle,
                currentUrl
        )

        activity?.also {
            ViewModelProvider(it)
                    .get(ContentViewModel::class.java)
                    .nextFragment(fragment)
        }
    }

    fun stopSwipeRefresherLoading() {
        //TODO binding?.swipeRefresher?.isRefreshing = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        /*
         * jp.toastkid.yobidashi.d E/AndroidRuntime: FATAL EXCEPTION: main
         *     kotlin.UninitializedPropertyAccessException: lateinit property browserModule has not been initialize
         *         at jp.toastkid.yobidashi.browser.BrowserFragment.onSaveInstanceState(BrowserFragment.kt:470)
         *         at androidx.fragment.app.Fragment.performSaveInstanceState(Fragment.java:2863)
         *         at androidx.fragment.app.FragmentStateManager.saveBasicState(FragmentStateManager.java:434)
         */
        if (::browserModule.isInitialized) {
            browserModule.onSaveInstanceState(outState)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        stopSwipeRefresherLoading()
        browserModule.onPause()
    }

    override fun onDetach() {
        appBarViewModel?.show()
        browserModule.onDestroy()
        storagePermissionRequestLauncher.unregister()
        super.onDetach()
    }

}
