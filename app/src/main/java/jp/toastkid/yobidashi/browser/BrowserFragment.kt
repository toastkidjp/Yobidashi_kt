package jp.toastkid.yobidashi.browser

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInsertion
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.page_search.PageSearcherViewModel
import jp.toastkid.yobidashi.browser.reader.ReaderFragment
import jp.toastkid.yobidashi.browser.shortcut.ShortcutUseCase
import jp.toastkid.yobidashi.browser.user_agent.UserAgent
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDialogFragment
import jp.toastkid.yobidashi.databinding.AppBarBrowserBinding
import jp.toastkid.yobidashi.databinding.FragmentBrowserBinding
import jp.toastkid.lib.Urls
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.tab.TabUiFragment
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.rss.extractor.RssUrlFinder
import jp.toastkid.yobidashi.search.SearchFragment
import jp.toastkid.search.SearchQueryExtractor
import jp.toastkid.lib.TabListViewModel

/**
 * Internal browser fragment.
 *
 * @author toastkidjp
 */
class BrowserFragment : Fragment(),
        TabUiFragment,
        CommonFragmentAction,
        UserAgentDialogFragment.Callback,
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

    /**
     * Data binding object.
     */
    private var binding: FragmentBrowserBinding? = null

    private var appBarBinding: AppBarBrowserBinding? = null

    private val searchQueryExtractor = SearchQueryExtractor()

    private var appBarViewModel: AppBarViewModel? = null

    private var contentViewModel: ContentViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        appBarBinding = DataBindingUtil.inflate(inflater, APP_BAR_LAYOUT_ID, container, false)
        appBarBinding?.fragment = this

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding?.fragment = this

        binding?.swipeRefresher?.let {
            it.setOnRefreshListener { reload() }
            it.setOnChildScrollUpCallback { _, _ -> browserModule.disablePullToRefresh() }
            it.setDistanceToTriggerSync(500)
        }

        val activityContext = context ?: return null

        preferenceApplier = PreferenceApplier(activityContext)

        browserModule = BrowserModule(activityContext, binding?.webViewContainer)

        setHasOptionsMenu(true)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()

        initializeHeaderViewModels(activity)

        contentViewModel = ViewModelProvider(activity).get(ContentViewModel::class.java)
    }

    private fun initializeHeaderViewModels(activity: FragmentActivity) {
        val viewModelProvider = ViewModelProvider(activity)
        appBarViewModel = viewModelProvider.get(AppBarViewModel::class.java)

        viewModelProvider.get(BrowserHeaderViewModel::class.java).also { viewModel ->
            viewModel.stopProgress.observe(activity, Observer { stop ->
                if (!stop || binding?.swipeRefresher?.isRefreshing == false) {
                    return@Observer
                }
                stopSwipeRefresherLoading()
            })
            viewModel.progress.observe(activity, Observer { newProgress ->
                if (70 < newProgress) {
                    appBarBinding?.progress?.isVisible = false
                    appBarBinding?.reload?.setImageResource(R.drawable.ic_reload)
                    return@Observer
                }

                val progressTitle =
                        activity.getString(R.string.prefix_loading) + newProgress + "%"
                appBarBinding?.mainText?.text = progressTitle

                appBarBinding?.progress?.let {
                    it.isVisible = true
                    it.progress = newProgress
                    appBarBinding?.reload?.setImageResource(R.drawable.ic_close)
                }
            })
        }

        viewModelProvider.get(BrowserHeaderViewModel::class.java).also { viewModel ->
            viewModel.title.observe(activity, Observer { title ->
                if (title.isNullOrBlank()) {
                    return@Observer
                }
                appBarBinding?.mainText?.text = title
            })

            viewModel.url.observe(activity, Observer { url ->
                if (url.isNullOrBlank()) {
                    return@Observer
                }
                appBarBinding?.subText?.text = url
            })

            viewModel.reset.observe(activity, Observer {
                if (!isVisible) {
                    return@Observer
                }
                val headerView = appBarBinding?.root ?: return@Observer
                appBarViewModel?.replace(headerView)
            })

            viewModel.enableForward.observe(activity, Observer(::updateForwardButtonState))

            viewModel.enableBack.observe(activity, Observer(::updateBackButtonState))
        }

        viewModelProvider.get(LoadingViewModel::class.java)
                .onPageFinished
                .observe(
                        activity,
                        Observer { browserModule.saveArchiveForAutoArchive() }
                )

        ViewModelProvider(this).get(BrowserFragmentViewModel::class.java)
                .loadWithNewTab
                .observe(activity, Observer {
                    browserModule.loadWithNewTab(it.first, it.second)
                })

        viewModelProvider.get(TabListViewModel::class.java)
                .tabCount
                .observe(activity, Observer { appBarBinding?.tabCount?.text = it.toString() })

        viewModelProvider.get(PageSearcherViewModel::class.java).also { viewModel ->
            viewModel.find.observe(activity, Observer {
                browserModule.find(it)
            })

            viewModel.upward.observe(activity, Observer {
                browserModule.findUp()
            })

            viewModel.downward.observe(activity, Observer {
                browserModule.findDown()
            })
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.browser, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.translate -> {
                val uri = "https://translate.googleusercontent.com/translate_c" +
                        "?depth=1&nv=1&pto=aue&rurl=translate.google.com&sl=auto&sp=nmt4&tl=en&u=" +
                        Uri.encode(browserModule.currentUrl())
                ViewModelProvider(requireActivity()).get(BrowserViewModel::class.java)
                        .open(uri.toUri())
            }
            R.id.download_all_images -> {
                browserModule.downloadAllImages()
            }
            R.id.add_to_home -> {
                val uri = browserModule.currentUrl()?.toUri() ?: return true
                ShortcutUseCase(requireContext())
                        .invoke(
                                uri,
                                browserModule.currentTitle(),
                                FaviconApplier(requireContext()).load(uri)
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
                RssUrlFinder(preferenceApplier).invoke(browserModule.currentUrl()) { binding?.root }
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
                    contentViewModel?.snackShort(context.getString(R.string.message_replace_home_url))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun reload() {
        if (appBarBinding?.progress?.isVisible == true) {
            browserModule.stopLoading()
        } else {
            browserModule.reload()
        }
    }

    fun openReaderMode() {
        browserModule.invokeContentExtraction(ValueCallback(this::showReaderFragment))
    }

    fun tabList() {
        contentViewModel?.switchTabList()
    }

    private fun updateForwardButtonState(enable: Boolean) {
        appBarBinding?.forward?.isEnabled = enable
        appBarBinding?.forward?.alpha = if (enable) 1f else 0.6f
    }

    private fun updateBackButtonState(enable: Boolean) {
        appBarBinding?.back?.isEnabled = enable
        appBarBinding?.back?.alpha = if (enable) 1f else 0.6f
    }

    private fun showReaderFragment(content: String) {
        val lineSeparator = System.getProperty("line.separator") ?: ""
        val replacedContent = content.replace("\\n", lineSeparator)

        val readerFragment =
                activity?.supportFragmentManager?.findFragmentByTag(ReaderFragment::class.java.canonicalName)
                        ?: ReaderFragment()
        (readerFragment as? ReaderFragment)?.setContent(browserModule.currentTitle(), replacedContent)

        contentViewModel?.nextFragment(readerFragment)
    }

    fun showUserAgentSetting() {
        val dialogFragment = UserAgentDialogFragment()
        dialogFragment.setTargetFragment(this, 1)
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
        browserModule.invokeHtmlSourceExtraction(
                ValueCallback {
                    showReaderFragment(it.replace("\\u003C", "<"))
                }
        )
    }

    /**
     * Do browser back action.
     */
    fun back(): Boolean = browserModule.back()

    /**
     * Do browser forward action.
     */
    fun forward() = browserModule.forward()

    /**
     * TODO implement ViewModel.
     */
    fun search() {
        val currentTitle = browserModule.currentTitle()
        val currentUrl = browserModule.currentUrl()
        val query = searchQueryExtractor.invoke(currentUrl)
        val makeIntent = if (TextUtils.isEmpty(query) || Urls.isValidUrl(query)) {
            SearchFragment.makeWith(currentTitle, currentUrl)
        } else {
            SearchFragment.makeWithQuery(query ?: "", currentTitle, currentUrl)
        }

        activity?.also  { activity ->
            ViewModelProvider(activity)
                    .get(ContentViewModel::class.java)
                    .nextFragment(makeIntent)
        }
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

        val colorPair = colorPair()

        val fontColor = colorPair.fontColor()

        appBarBinding?.also {
            it.icon.setColorFilter(fontColor)
            it.mainText.setTextColor(fontColor)
            it.subText.setTextColor(fontColor)
            it.reload.setColorFilter(fontColor)
            it.back.setColorFilter(fontColor)
            it.forward.setColorFilter(fontColor)
            it.tabIcon.setColorFilter(fontColor)
            it.tabCount.setTextColor(fontColor)
            it.pageInformation.setColorFilter(fontColor)
            it.userAgent.setColorFilter(fontColor)
            it.htmlSource.setColorFilter(fontColor)
        }

        browserModule.resizePool(preferenceApplier.poolSize)
        browserModule.applyNewAlpha()
        browserModule.onResume()

        binding?.swipeRefresher?.let {
            it.setProgressBackgroundColorSchemeColor(preferenceApplier.color)
            it.setColorSchemeColors(preferenceApplier.fontColor)
        }
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
                IntentFactory.makeShare(browserModule.makeShareMessage())
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

    /**
     * TODO delete it.
     * Hide option menus.
     */
    private fun hideOption(): Boolean {
        return false
    }
    
    private fun colorPair() = preferenceApplier.colorPair()

    override fun onClickUserAgent(userAgent: UserAgent) {
        browserModule.resetUserAgent(userAgent.text())
        contentViewModel?.snackShort(getString(R.string.format_result_user_agent, userAgent.title()))
    }

    fun stopSwipeRefresherLoading() {
        binding?.swipeRefresher?.isRefreshing = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        browserModule.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        stopSwipeRefresherLoading()
        browserModule.onPause()
    }

    override fun onDetach() {
        appBarViewModel?.show()
        browserModule.onPause()
        super.onDetach()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_browser

        @LayoutRes
        private const val APP_BAR_LAYOUT_ID = R.layout.app_bar_browser

    }

}
