package jp.toastkid.yobidashi.browser

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import androidx.annotation.LayoutRes
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
import jp.toastkid.yobidashi.browser.user_agent.UserAgent
import jp.toastkid.yobidashi.browser.user_agent.UserAgentDialogFragment
import jp.toastkid.yobidashi.databinding.FragmentBrowserBinding
import jp.toastkid.yobidashi.databinding.ModuleBrowserHeaderBinding
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.ContentScrollable
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.main.TabUiFragment
import jp.toastkid.yobidashi.main.content.ContentViewModel
import jp.toastkid.yobidashi.menu.MenuViewModel
import jp.toastkid.yobidashi.rss.extractor.RssUrlFinder
import jp.toastkid.yobidashi.search.SearchFragment
import jp.toastkid.yobidashi.search.SearchQueryExtractor
import jp.toastkid.yobidashi.tab.tab_list.TabListViewModel

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

    private var headerBinding: ModuleBrowserHeaderBinding? = null

    private val searchQueryExtractor = SearchQueryExtractor()

    private var menuViewModel: MenuViewModel? = null

    private var headerViewModel: HeaderViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

    private var browserFragmentViewModel: BrowserFragmentViewModel? = null

    private var contentViewModel: ContentViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        headerBinding = DataBindingUtil.inflate(inflater, R.layout.module_browser_header, container, false)
        headerBinding?.fragment = this

        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding?.fragment = this

        binding?.swipeRefresher?.let {
            it.setOnRefreshListener { reload() }
            it.setOnChildScrollUpCallback { _, _ -> browserModule.disablePullToRefresh() }
            it.setDistanceToTriggerSync(8000)
            it.setSlingshotDistance(150)
        }

        val activityContext = context ?: return null

        preferenceApplier = PreferenceApplier(activityContext)

        browserModule = BrowserModule(activityContext, binding?.webViewContainer)

        setHasOptionsMenu(true)

        browserViewModel = ViewModelProvider(this).get(BrowserViewModel::class.java)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        menuViewModel = ViewModelProvider(activity)
                .get(MenuViewModel::class.java)

        initializeHeaderViewModels(activity)

        contentViewModel = ViewModelProvider(activity).get(ContentViewModel::class.java)
    }

    private fun initializeHeaderViewModels(activity: FragmentActivity) {
        val viewModelProvider = ViewModelProvider(activity)
        headerViewModel = viewModelProvider.get(HeaderViewModel::class.java)

        headerViewModel?.stopProgress?.observe(activity, Observer { stop ->
            if (!stop || binding?.swipeRefresher?.isRefreshing == false) {
                return@Observer
            }
            stopSwipeRefresherLoading()
        })

        headerViewModel?.progress?.observe(activity, Observer { newProgress ->
            if (70 < newProgress) {
                headerBinding?.progress?.isVisible = false
                headerBinding?.reload?.setImageResource(R.drawable.ic_reload)
                //TODO refreshThumbnail()
                return@Observer
            }
            headerBinding?.progress?.let {
                it.isVisible = true
                it.progress = newProgress
                headerBinding?.reload?.setImageResource(R.drawable.ic_close)
            }
        })

        viewModelProvider.get(BrowserHeaderViewModel::class.java).also { viewModel ->
            viewModel.title.observe(activity, Observer { title ->
                if (title.isNullOrBlank()) {
                    return@Observer
                }
                headerBinding?.mainText?.text = title
            })

            viewModel.url.observe(activity, Observer { url ->
                if (url.isNullOrBlank()) {
                    return@Observer
                }
                headerBinding?.subText?.text = url
            })

            viewModel.reset.observe(activity, Observer {
                if (!isVisible) {
                    return@Observer
                }
                val headerView = headerBinding?.root ?: return@Observer
                headerViewModel?.replace(headerView)
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

        browserFragmentViewModel = viewModelProvider.get(BrowserFragmentViewModel::class.java)
        browserFragmentViewModel
                ?.loadWithNewTab
                ?.observe(activity, Observer {
                    browserModule.loadWithNewTab(it.first, it.second)
                })

        viewModelProvider.get(TabListViewModel::class.java)
                .tabCount
                .observe(activity, Observer { headerBinding?.tabCount?.text = it.toString() })

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
        if (headerBinding?.progress?.isVisible == true) {
            browserModule.stopLoading()
        } else {
            browserModule.reload()
        }
    }

    fun openReaderMode() {
        browserModule.invokeContentExtraction(ValueCallback(this::showReaderFragment))
    }

    // TODO should implement view model.
    fun tabList() {
        val mainActivity = activity as? MainActivity
        mainActivity?.switchTabList()
    }

    private fun updateForwardButtonState(enable: Boolean) {
        headerBinding?.forward?.isEnabled = enable
        headerBinding?.forward?.alpha = if (enable) 1f else 0.6f
    }

    private fun updateBackButtonState(enable: Boolean) {
        headerBinding?.back?.isEnabled = enable
        headerBinding?.back?.alpha = if (enable) 1f else 0.6f
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

        headerBinding?.also {
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
        when (preferenceApplier.browserScreenMode()) {
            ScreenMode.FULL_SCREEN -> headerViewModel?.hide()
            ScreenMode.EXPANDABLE, ScreenMode.FIXED -> headerViewModel?.show()
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

    override fun onPause() {
        super.onPause()
        stopSwipeRefresherLoading()
        browserModule.onPause()
    }

    override fun onDetach() {
        headerViewModel?.show()
        browserModule.dispose()
        super.onDetach()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val layoutId = R.layout.fragment_browser

    }

}
