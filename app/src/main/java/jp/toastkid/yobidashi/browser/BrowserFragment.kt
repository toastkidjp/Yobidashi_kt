package jp.toastkid.yobidashi.browser

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.processors.PublishProcessor
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.TitlePair
import jp.toastkid.yobidashi.barcode.BarcodeReaderActivity
import jp.toastkid.yobidashi.browser.archive.Archive
import jp.toastkid.yobidashi.browser.archive.ArchivesActivity
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.browser.tab.TabAdapter
import jp.toastkid.yobidashi.browser.tab.TabListModule
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.FragmentBrowserBinding
import jp.toastkid.yobidashi.databinding.ModuleTabListBinding
import jp.toastkid.yobidashi.home.Command
import jp.toastkid.yobidashi.home.FragmentReplaceAction
import jp.toastkid.yobidashi.libs.ImageCache
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingsActivity
import java.io.File
import java.io.IOException

/**
 * Internal browser fragment.

 * @author toastkidjp
 */
class BrowserFragment : BaseFragment() {

    /** Data binding object.  */
    private var binding: FragmentBrowserBinding? = null

    /** Archive folder.  */
    private lateinit var tabs: TabAdapter

    private var fragmentReplaceAction: FragmentReplaceAction? = null

    private var tabListModule: TabListModule? = null

    private var pageSearcherModule: PageSearcherModule? = null

    /** Title processor  */
    private val titleProcessor: PublishProcessor<TitlePair>

    /** Disposer.  */
    private val disposables: CompositeDisposable

    private lateinit var searchWithClip: SearchWithClip

    init {
        titleProcessor = PublishProcessor.create<TitlePair>()
        disposables = CompositeDisposable()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentReplaceAction = activity as FragmentReplaceAction
    }

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentBrowserBinding>(
                inflater!!, R.layout.fragment_browser, container, false)
        binding!!.fragment = this

        tabs = TabAdapter(
                binding?.progress as ProgressBar,
                binding?.webViewContainer as FrameLayout,
                Consumer<TitlePair> { titleProcessor.onNext(it) },
                { this.hideOption() },
                { fragmentReplaceAction?.action(Command.OPEN_HOME) }
        )

        initMenus()

        pageSearcherModule = PageSearcherModule(binding!!.sip, tabs)

        val cm = activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        searchWithClip = SearchWithClip(
                cm,
                binding!!.root,
                colorPair(),
                { url -> tabs.loadWithNewTab(Uri.parse(url)) }
        )
        searchWithClip.invoke()

        val url = arguments.getParcelable<Uri>("url")
        if (url != null) {
            tabs.loadWithNewTab(url)
        }

        return binding!!.root
    }

    /**
     * Initialize menus view.
     */
    private fun initMenus() {
        binding!!.menusView.adapter = Adapter(activity, Consumer<Menu> { this.onMenuClick(it) })
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding!!.menusView.layoutManager = layoutManager
        layoutManager.scrollToPosition(Adapter.mediumPosition())
    }

    /**
     * Show quick control menu.

     * @param view
     */
    fun showMenu(view: View) {
        binding!!.fab.hide()
        binding!!.menusView.visibility = View.VISIBLE
    }

    /**
     * Hide quick control menu.
     */
    private fun hideMenu() {
        binding!!.fab.show()
        binding!!.menusView.visibility = View.GONE
    }

    /**
     * Menu action.
     * @param menu
     */
    private fun onMenuClick(menu: Menu) {
        val context = activity
        when (menu) {
            Menu.RELOAD -> {
                tabs.reload()
                return
            }
            Menu.BACK -> {
                back()
                return
            }
            Menu.FORWARD -> {
                val forward = tabs.forward()
                if (forward.isNotEmpty()) {
                    tabs.loadUrl(forward)
                }
                return
            }
            Menu.TOP -> {
                tabs.pageUp()
                return
            }
            Menu.BOTTOM -> {
                tabs.pageDown()
                return
            }
            Menu.CLOSE -> {
                hideMenu()
                return
            }
            Menu.FIND_IN_PAGE -> {
                if (pageSearcherModule!!.isVisible) {
                    pageSearcherModule!!.hide()
                    return
                }
                pageSearcherModule!!.show(activity)
                hideMenu()
                return
            }
            Menu.SCREENSHOT -> {
                tabs.currentSnap()
                Toaster.snackShort(binding!!.root, R.string.message_done_save, colorPair())
                return
            }
            Menu.SHARE -> {
                startActivity(
                        IntentFactory.makeShare(tabs.currentTitle()
                                + System.getProperty("line.separator") + tabs.currentUrl())
                )
                return
            }
            Menu.SETTING -> {
                startActivity(SettingsActivity.makeIntent(context))
                return
            }
            Menu.USER_AGENT -> {
                UserAgent.showSelectionDialog(binding!!.root, Consumer<UserAgent> { tabs.resetUserAgent(it) })
                return
            }
            Menu.WIFI_SETTING -> {
                startActivity(SettingsIntentFactory.wifi())
                return
            }
            Menu.CLEAR_CACHE -> {
                AlertDialog.Builder(context)
                        .setTitle(R.string.title_clear_cache)
                        .setMessage(Html.fromHtml(context.getString(R.string.confirm_clear_all_settings)))
                        .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                        .setPositiveButton(R.string.ok) { d, i ->
                            tabs.clearCache()
                            Toaster.snackShort(binding!!.root, R.string.done_clear, colorPair())
                            d.dismiss()
                        }
                        .setCancelable(true)
                        .show()
                return
            }
            Menu.CLEAR_FORM_DATA -> {
                AlertDialog.Builder(context)
                        .setTitle(R.string.title_clear_form_data)
                        .setMessage(Html.fromHtml(context.getString(R.string.confirm_clear_all_settings)))
                        .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                        .setPositiveButton(R.string.ok) { d, i ->
                            tabs.clearFormData()
                            Toaster.snackShort(binding!!.root, R.string.done_clear, colorPair())
                            d.dismiss()
                        }
                        .setCancelable(true)
                        .show()
                return
            }
            Menu.PAGE_INFORMATION -> {
                tabs.showPageInformation()
                return
            }
            Menu.TAB_LIST -> {
                if (tabListModule == null) {
                    tabListModule = TabListModule(
                            DataBindingUtil.inflate<ModuleTabListBinding>(
                                LayoutInflater.from(activity), R.layout.module_tab_list, null, false),
                            tabs,
                            binding?.root as View,
                            this::hideTabList
                    )
                    binding?.tabListContainer?.addView(tabListModule?.moduleView)
                }

                if (tabListModule?.isVisible as Boolean) {
                    hideTabList()
                } else {
                    hideMenu()
                    binding?.fab?.hide()
                    tabListModule?.show()
                }
                return
            }
            Menu.OPEN -> {
                val inputLayout = TextInputs.make(context)
                inputLayout.editText?.setText(tabs.currentUrl())
                AlertDialog.Builder(context)
                        .setTitle(R.string.title_open_url)
                        .setView(inputLayout)
                        .setCancelable(true)
                        .setPositiveButton("開く") { d, i ->
                            val url = inputLayout.editText!!.text.toString()
                            if (Urls.isValidUrl(url)) {
                                tabs.loadWithNewTab(Uri.parse(url))
                            }
                        }
                        .show()
                return
            }
            Menu.OTHER_BROWSER -> {
                startActivity(IntentFactory.openBrowser(Uri.parse(tabs.currentUrl())))
                return
            }
            Menu.CHROME_TAB -> {
                CustomTabsFactory.make(context, colorPair(), R.drawable.ic_back)
                        .build()
                        .launchUrl(context, Uri.parse(tabs.currentUrl()))
                return
            }
            Menu.BARCODE_READER -> {
                startActivity(BarcodeReaderActivity.makeIntent(context))
                return
            }
            Menu.SHARE_BARCODE -> {
                val imageView = ImageView(context)
                try {
                    val bitmap = BarcodeEncoder()
                            .encodeBitmap(tabs.currentUrl(), BarcodeFormat.QR_CODE, 400, 400)
                    imageView.setImageBitmap(bitmap)
                    AlertDialog.Builder(context)
                            .setTitle(R.string.title_share_by_code)
                            .setView(imageView)
                            .setCancelable(true)
                            .setPositiveButton(R.string.share) { d, i ->
                                val uri = FileProvider.getUriForFile(
                                        context,
                                        BuildConfig.APPLICATION_ID + ".fileprovider",
                                        ImageCache.saveBitmap(context, bitmap).absoluteFile
                                )
                                startActivity(IntentFactory.shareImage(uri))
                                d.dismiss()
                            }
                            .show()
                } catch (e: WriterException) {
                    e.printStackTrace()
                    Toaster.snackShort(binding!!.root, e.message.orEmpty(), colorPair())
                    return
                }

                return
            }
            Menu.ARCHIVE -> {
                tabs.saveArchive()
                return
            }
            Menu.VIEW_ARCHIVE -> {
                if (Archive.cannotUseArchive()) {
                    Toaster.snackShort(binding!!.root, R.string.message_disable_archive, colorPair())
                    return
                }
                startActivityForResult(ArchivesActivity.makeIntent(context), REQUEST_CODE_VIEW_ARCHIVE)
                return
            }
            Menu.SEARCH -> {
                fragmentReplaceAction!!.action(Command.OPEN_SEARCH)
                return
            }
            Menu.SITE_SEARCH -> {
                tabs.siteSearch()
                return
            }
            Menu.VOICE_SEARCH -> {
                startActivityForResult(VoiceSearch.makeIntent(context), REQUEST_CODE_VOICE_SEARCH)
                return
            }
            Menu.COLOR_FILTER -> {
                ColorFilter(activity, binding!!.root).switchState(this, REQUEST_OVERLAY_PERMISSION)
                return
            }
            Menu.EXIT -> {
                activity.finish()
                return
            }
            else -> return
        }
    }

    private fun back(): Boolean {
        val back = tabs.back()
        if (back.isNotEmpty()) {
            tabs.loadUrl(back)
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()

        refreshFab()

        disposables.add(tabs.reloadWebViewSettings())
    }

    private fun refreshFab() {
        val preferenceApplier = preferenceApplier() as PreferenceApplier
        binding!!.fab.setBackgroundColor(preferenceApplier.colorPair().bgColor())

        val resources = resources
        val fabMarginBottom = resources.getDimensionPixelSize(R.dimen.fab_margin)
        val fabMarginHorizontal = resources.getDimensionPixelSize(R.dimen.fab_margin_horizontal)
        MenuPos.place(binding!!.fab, fabMarginBottom, fabMarginHorizontal, preferenceApplier.menuPos())

        tabs.resetUserAgent(UserAgent.valueOf(preferenceApplier.userAgent()))
    }

    override fun pressBack(): Boolean {
        return hideOption() || back()
    }

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
        tabListModule?.hide()
        binding?.fab?.show()
    }

    override fun titleId(): Int {
        return R.string.title_browser
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        when (requestCode) {
            REQUEST_CODE_VIEW_ARCHIVE -> {
                try {
                    tabs.loadArchive(File(data.getStringExtra("FILE_NAME")))
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (error: OutOfMemoryError) {
                    error.printStackTrace()
                    System.gc()
                }

                return
            }
            REQUEST_CODE_VOICE_SEARCH -> {
                VoiceSearch.processResult(activity, data)
                return
            }
            REQUEST_OVERLAY_PERMISSION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                    Toaster.snackShort(
                            binding!!.root,
                            R.string.message_cannot_draw_overlay,
                            colorPair()
                    )
                    return
                }
                ColorFilter(activity, binding!!.root)
                        .switchState(this, REQUEST_OVERLAY_PERMISSION)
                return
            }
        }
    }

    fun titlePairProcessor(): PublishProcessor<TitlePair> {
        return titleProcessor
    }

    override fun onDestroy() {
        super.onDestroy()
        (binding!!.menusView.adapter as Adapter).dispose()
        tabs.dispose()
        disposables.dispose()
        searchWithClip.dispose();
    }

    companion object {

        private val REQUEST_CODE_VIEW_ARCHIVE = 1

        private val REQUEST_CODE_VOICE_SEARCH = 2

        private val REQUEST_OVERLAY_PERMISSION = 3
    }
}
