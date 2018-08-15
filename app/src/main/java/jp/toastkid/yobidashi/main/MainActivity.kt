package jp.toastkid.yobidashi.main

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.about.AboutThisAppActivity
import jp.toastkid.yobidashi.barcode.BarcodeReaderActivity
import jp.toastkid.yobidashi.barcode.InstantBarcodeGenerator
import jp.toastkid.yobidashi.barcode.LinearBarcodeReader
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.ProgressBarCallback
import jp.toastkid.yobidashi.browser.TitlePair
import jp.toastkid.yobidashi.browser.archive.Archive
import jp.toastkid.yobidashi.browser.archive.ArchivesActivity
import jp.toastkid.yobidashi.browser.bookmark.BookmarkActivity
import jp.toastkid.yobidashi.browser.history.ViewHistoryActivity
import jp.toastkid.yobidashi.browser.screenshots.ScreenshotsActivity
import jp.toastkid.yobidashi.calendar.CalendarArticleLinker
import jp.toastkid.yobidashi.calendar.CalendarFragment
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.ActivityMainBinding
import jp.toastkid.yobidashi.home.Command
import jp.toastkid.yobidashi.home.FragmentReplaceAction
import jp.toastkid.yobidashi.home.HomeFragment
import jp.toastkid.yobidashi.launcher.LauncherActivity
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.intent.ImplicitIntentInvokerDialogFragment
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.planning_poker.PlanningPokerActivity
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.search.favorite.AddingFavoriteSearchService
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchActivity
import jp.toastkid.yobidashi.search.history.SearchHistoryActivity
import jp.toastkid.yobidashi.settings.SettingsActivity
import jp.toastkid.yobidashi.torch.Torch
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.MessageFormat

/**
 * Main of this calendar app.
 *
 * @author toastkidjp
 */
class MainActivity : BaseActivity(), FragmentReplaceAction, ToolbarAction, ProgressBarCallback {

    /**
     * Navigation's background.
     */
    private var navBackground: View? = null

    /**
     * Data binding object.
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * Browser fragment.
     */
    private lateinit var browserFragment: BrowserFragment

    /**
     * Home fragment.
     */
    private val homeFragment by lazy { HomeFragment() }

    /**
     * Calendar fragment.
     */
    private val calendarFragment by lazy { CalendarFragment() }

    /**
     * Disposables.
     */
    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    /**
     * Use for delaying.
     */
    private val uiThreadHandler = Handler(Looper.getMainLooper())

    /**
     * Torch API facade.
     */
    private val torch by lazy { Torch(this) }

    /**
     * RxPermissions.
     */
    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)

        binding.appBarMain?.toolbar?.let {
            initToolbar(it)
            setSupportActionBar(it)
            initDrawer(it)
            it.setOnClickListener { findCurrentFragment()?.tapHeader() }
        }

        initNavigation()

        browserFragment = BrowserFragment()

        if (preferenceApplier.useColorFilter()) {
            ColorFilter(this, binding.root).start()
        }

        processShortcut(intent)
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
            return
        }

        when (calledIntent.action) {
            Intent.ACTION_VIEW -> {
                calledIntent.data?.let { loadUri(it, true) }
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
        }

        if (calledIntent.hasExtra(KEY_EXTRA_MONTH)) {
            CalendarArticleLinker(
                    this,
                    calledIntent.getIntExtra(KEY_EXTRA_MONTH, -1),
                    calledIntent.getIntExtra(KEY_EXTRA_DOM, -1)
            ).invoke()
            return
        }

        when (preferenceApplier.startUp) {
            StartUp.START -> {
                replaceFragment(homeFragment)
            }
            StartUp.APPS_LAUNCHER -> {
                startActivity(LauncherActivity.makeIntent(this))
                finish()
            }
            StartUp.BROWSER -> {
                replaceWithBrowser(Uri.EMPTY)
            }
            StartUp.SEARCH -> {
                startActivity(SearchActivity.makeIntent(this))
            }
        }

    }

    override fun onProgressChanged(newProgress: Int) {
        if (70 < newProgress) {
            binding.appBarMain?.progress?.visibility = View.GONE
            return
        }
        binding.appBarMain?.progress?.let {
            it.visibility = View.VISIBLE
            it.progress = newProgress
        }
    }

    override fun onTitleChanged(titlePair: TitlePair) {
        binding.appBarMain?.toolbar?.let {
            it.title    = titlePair.title()
            it.subtitle = titlePair.subtitle()
        }
    }

    /**
     * Load Uri.
     *
     * @param uri
     * @param shouldLoadInternal for avoiding infinite loop
     */
    private fun loadUri(uri: Uri, shouldLoadInternal: Boolean = false) {
        if (preferenceApplier.useInternalBrowser() || shouldLoadInternal) {
            if (browserFragment.isVisible) {
                browserFragment.loadWithNewTab(uri)
                return
            }
            replaceWithBrowser(uri)
            return
        }
        CustomTabsFactory
                .make(this, colorPair())
                .build()
                .launchUrl(this, uri)
    }

    /**
     * Replace with [BrowserFragment].
     *
     * @param uri default empty.
     */
    private fun replaceWithBrowser(uri: Uri = Uri.EMPTY) {
        replaceFragment(browserFragment)
        if (uri != Uri.EMPTY) {
            uiThreadHandler.postDelayed({ browserFragment.loadWithNewTab(uri) }, 200L)
        }
    }

    /**
     * Replace with passed fragment.
     *
     * @param fragment {@link BaseFragment} instance
     */
    private fun replaceFragment(fragment: BaseFragment) {

        if (fragment.isVisible) {
            snackSuppressOpenFragment()
            return
        }

        val transaction = supportFragmentManager.beginTransaction()
        val fragments = supportFragmentManager?.fragments
        if (fragments?.size != 0) {
            fragments?.get(0)?.let { transaction.remove(it) }
        }
        transaction.setCustomAnimations(R.anim.slide_in_right, 0, 0, android.R.anim.slide_out_right)
        transaction.add(R.id.content, fragment, fragment::class.java.simpleName)
        transaction.commitAllowingStateLoss()
        binding.drawerLayout.closeDrawers()
        binding.appBarMain?.toolbar?.let {
            it.setTitle(fragment.titleId())
            it.subtitle = ""
        }
    }

    /**
     * Initialize drawer.
     *
     * @param toolbar
     */
    private fun initDrawer(toolbar: Toolbar) {
        val toggle = ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    /**
     * Initialize navigation.
     */
    private fun initNavigation() {
        binding.navView.setNavigationItemSelectedListener { item: MenuItem ->
            invokeWithMenuId(item.itemId)
            true
        }
        if (Archive.cannotUseArchive()) {
            binding.navView.menu.findItem(R.id.nav_archives).isVisible = false
        }
        val headerView = binding.navView.getHeaderView(0)
        navBackground = headerView?.findViewById(R.id.nav_header_background)
    }

    /**
     * Invoke action with Menu ID.
     *
     * @param menuId Menu ID
     */
    private fun invokeWithMenuId(@IdRes menuId: Int) {
        when (menuId) {
            R.id.nav_search -> {
                startActivityWithSlideIn("nav_search", SearchActivity.makeIntent(this))
            }
            R.id.nav_search_history -> {
                startActivityWithSlideIn("nav_srch_hstry", SearchHistoryActivity.makeIntent(this))
            }
            R.id.nav_calendar -> {
                sendLog("nav_cal")
                replaceFragment(calendarFragment)
            }
            R.id.nav_favorite_search -> {
                startActivityWithSlideIn(
                        "nav_fav_search", FavoriteSearchActivity.makeIntent(this))
            }
            R.id.nav_intent_invoker -> {
                sendLog("nav_intnt")
                ImplicitIntentInvokerDialogFragment().show(
                        supportFragmentManager,
                        ImplicitIntentInvokerDialogFragment::class.java.simpleName
                )
            }
            R.id.nav_color_filter -> {
                ColorFilter(this, binding.root).switchState(this)
            }
            R.id.nav_launcher -> {
                startActivityWithSlideIn("nav_lnchr", LauncherActivity.makeIntent(this))
            }
            R.id.nav_share -> {
                sendLog("nav_shr")
                startActivity(IntentFactory.makeShare(makeShareMessage()))
            }
            R.id.nav_share_twitter -> {
                sendLog("nav_shr_twt")
                IntentFactory.makeTwitter(this@MainActivity, colorPair())
                        .launchUrl(
                        this@MainActivity,
                        Uri.parse("https://twitter.com/share?text=" + Uri.encode(makeShareMessage()))
                )
            }
            R.id.nav_about_this_app -> {
                startActivityWithSlideIn("nav_about", AboutThisAppActivity.makeIntent(this))
            }
            R.id.nav_archives -> {
                if (Archive.cannotUseArchive()) {
                    Toaster.snackShort(binding.root, R.string.message_disable_archive, colorPair())
                    return
                }
                startActivityForResultWithSlideIn(
                        "nav_archv",
                        ArchivesActivity.makeIntent(this),
                        ArchivesActivity.REQUEST_CODE
                )
            }
            R.id.nav_screenshots -> {
                startActivityWithSlideIn("nav_screenshots", ScreenshotsActivity.makeIntent(this))
            }
            R.id.nav_google_play -> {
                sendLog("nav_gplay")
                startActivity(IntentFactory.googlePlay(BuildConfig.APPLICATION_ID))
            }
            R.id.nav_privacy_policy -> {
                sendLog("nav_prvcy_plcy")
                CustomTabsFactory.make(this, colorPair())
                        .build()
                        .launchUrl(this, Uri.parse(getString(R.string.link_privacy_policy)))
            }
            R.id.nav_author -> {
                sendLog("nav_author")
                startActivity(IntentFactory.authorsApp())
            }
            R.id.nav_option_menu -> {
                binding.appBarMain?.toolbar?.showOverflowMenu()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_settings -> {
                startActivityWithSlideIn("nav_set_top", SettingsActivity.makeIntent(this))
            }
            R.id.nav_planning_poker -> {
                startActivityWithSlideIn("nav_poker", PlanningPokerActivity.makeIntent(this))
            }
            R.id.nav_camera -> {
                sendLog("nav_camera")
                useCameraPermission { startActivityWithSlideIn("nav_camera", IntentFactory.camera()) }
            }
            R.id.nav_bookmark -> {
                startActivityForResultWithSlideIn(
                        "nav_bkmk",
                        BookmarkActivity.makeIntent(this),
                        BookmarkActivity.REQUEST_CODE
                )
            }
            R.id.nav_view_history -> {
                startActivityForResultWithSlideIn(
                        "nav_view_history",
                        ViewHistoryActivity.makeIntent(this),
                        ViewHistoryActivity.REQUEST_CODE
                )
            }
            R.id.nav_torch -> {
                sendLog("nav_torch")
                useCameraPermission { torch.switch() }
            }
            R.id.nav_barcode -> {
                sendLog("nav_barcode")
                startActivity(BarcodeReaderActivity.makeIntent(this))
            }
            R.id.nav_instant_barcode -> {
                sendLog("nav_instant_barcode")
                InstantBarcodeGenerator(this).invoke()
            }
            R.id.nav_linear_barcode -> {
                LinearBarcodeReader(this)
            }
            R.id.nav_home -> {
                sendLog("nav_home")
                replaceFragment(homeFragment)
            }
        }
    }

    /**
     * Use camera permission with specified action.
     *
     * @param onGranted action
     */
    private fun useCameraPermission(onGranted: () -> Unit) {
        rxPermissions
                .request(Manifest.permission.CAMERA)
                .filter { it }
                .subscribe(
                        { onGranted() },
                        { Timber.e(it) }
                )
                .addTo(disposables)
    }

    /**
     * Start activity with slide in transition.
     *
     * @param logKey
     * @param intent activity launcher intent
     */
    private fun startActivityWithSlideIn(logKey: String, intent: Intent) {
        sendLog(logKey)
        startActivity(intent)
        insertSlideInTransition()
    }

    /**
     * Start activity for result with slide in transition.
     *
     * @param logKey
     * @param intent activity launcher intent
     * @param requestCode Request code
     */
    private fun startActivityForResultWithSlideIn(
            logKey: String,
            intent: Intent,
            requestCode: Int
            ) {
        sendLog(logKey)
        startActivityForResult(intent, requestCode)
        insertSlideInTransition()
    }

    /**
     * Insert slide-in transition.
     */
    private fun insertSlideInTransition() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            return findCurrentFragment()?.pressLongBack() ?: super.onKeyLongPress(keyCode, event)
        }
        return super.onKeyLongPress(keyCode, event)
     }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        val fragment: BaseFragment? = findCurrentFragment()
        if (fragment == null) {
            confirmExit()
            return
        }

        if (fragment.pressBack()) {
            return
        }

        confirmExit()
    }

    /**
     * Find current fragment.
     *
     * @return fragment or null
     */
    private fun findCurrentFragment(): BaseFragment? {
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.content)

        return if (fragment != null) fragment as BaseFragment else null
    }

    /**
     * Show confirm exit.
     */
    private fun confirmExit() {
        CloseDialogFragment()
                .show(supportFragmentManager, CloseDialogFragment::class.java.simpleName)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.common, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.settings_toolbar_menu_exit) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * Refresh toolbar and background.
     */
    private fun refresh() {
        applyColorToToolbar(binding.appBarMain?.toolbar as Toolbar)

        applyBackgrounds()
    }

    /**
     * Apply background appearance.
     */
    private fun applyBackgrounds() {
        val backgroundImagePath = backgroundImagePath
        val fontColor = colorPair().fontColor()
        navBackground?.setBackgroundColor(colorPair().bgColor())
        if (backgroundImagePath.isEmpty()) {
            setBackgroundImage(null)
            navBackground?.findViewById<TextView>(R.id.nav_header_main)?.setTextColor(fontColor)
            return
        }

        try {
            setBackgroundImage(
                    ImageLoader.readBitmapDrawable(
                            this,
                            Uri.parse(File(backgroundImagePath).toURI().toString())
                    )
            )
        } catch (e: IOException) {
            Timber.e(e)
            Toaster.snackShort(
                    navBackground!!,
                    getString(R.string.message_failed_read_image),
                    colorPair()
            )
            removeBackgroundImagePath()
            setBackgroundImage(null)
        }

        navBackground?.findViewById<TextView>(R.id.nav_header_main)?.setTextColor(fontColor)
    }

    /**
     * Set background image.
     *
     * @param background nullable
     */
    private fun setBackgroundImage(background: BitmapDrawable?) {
        binding.drawerBackground.setImageDrawable(background)
        binding.appBarMain?.background?.setImageDrawable(background)
    }

    override fun action(c: Command) {
        when (c) {
            Command.OPEN_BROWSER -> {
                replaceWithBrowser()
                return
            }
            Command.OPEN_SEARCH -> {
                startActivity(SearchActivity.makeIntent(this))
                return
            }
        }
    }

    override fun hideToolbar() {
        binding.appBarMain?.toolbar?.animate()?.let {
            it.cancel()
            it.translationY(-resources.getDimension(R.dimen.toolbar_height))
                    .setDuration(HEADER_HIDING_DURATION)
                    .withStartAction { binding.appBarMain?.content?.requestLayout() }
                    .withEndAction   { binding.appBarMain?.toolbar?.visibility = View.GONE }
                    .start()
        }
    }

    override fun showToolbar() {
        binding.appBarMain?.toolbar?.animate()?.let {
            it.cancel()
            it.translationY(0f)
                    .setDuration(HEADER_HIDING_DURATION)
                    .withStartAction { binding.appBarMain?.toolbar?.visibility = View.VISIBLE }
                    .withEndAction   { binding.appBarMain?.content?.requestLayout() }
                    .start()
        }
    }

    /**
     * Show snackbar with confirm message of suppressed replacing fragment.
     */
    private fun snackSuppressOpenFragment() {
        Toaster.snackShort(binding.root, R.string.message_has_opened_fragment, colorPair())
    }

    /**
     * Make share message.
     * @return string
     */
    private fun makeShareMessage(): String
            = MessageFormat.format(getString(R.string.message_share), getString(R.string.app_name))

    @StringRes
    override fun titleId(): Int = R.string.app_name

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        when (requestCode) {
            ViewHistoryActivity.REQUEST_CODE, BookmarkActivity.REQUEST_CODE -> {
                data.data?.let { loadUri(it) }
            }
            ArchivesActivity.REQUEST_CODE -> {
                try {
                    replaceWithBrowser()
                    uiThreadHandler.postDelayed(
                            { browserFragment.loadArchive(
                                    File(data.getStringExtra(ArchivesActivity.EXTRA_KEY_FILE_NAME)))},
                            200L
                    )
                } catch (e: IOException) {
                    Timber.e(e)
                } catch (error: OutOfMemoryError) {
                    Timber.e(error)
                    System.gc()
                }
            }
            ColorFilter.REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                    Toaster.snackShort(
                            binding.root,
                            R.string.message_cannot_draw_overlay,
                            colorPair()
                    )
                    return
                }
                ColorFilter(this, binding.root).switchState(this)
            }
            IntentIntegrator.REQUEST_CODE -> {
                val result: IntentResult? =
                        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result?.contents == null) {
                    Toaster.snackShort(binding.root, "Cancelled", colorPair())
                    return
                }
                Toaster.snackLong(
                        binding.root,
                        "Scanned: ${result.contents}",
                        R.string.clip,
                        View.OnClickListener { Clipboard.clip(this, result.contents) },
                        colorPair()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        torch.dispose()
    }

    companion object {

        /**
         * Header hiding duration.
         */
        private const val HEADER_HIDING_DURATION: Long = 75L

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_main

        /**
         * For using daily alarm.
         */
        private const val KEY_EXTRA_MONTH: String = "month"

        /**
         * For using daily alarm.
         */
        private const val KEY_EXTRA_DOM: String = "dom"

        /**
         * Make launcher intent.
         *
         * @param context
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }

        /**
         * Make browser intent.
         *
         * @param context
         * @param uri
         *
         * @return [Intent]
         */
        fun makeBrowserIntent(context: Context, uri: Uri): Intent
                = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = uri
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

        /**
         * Make launcher intent.
         *
         * @param context
         * @param dayOfMonth
         * @return [Intent]
         */
        fun makeIntent(context: Context, month: Int, dayOfMonth: Int): Intent
                = makeIntent(context).apply {
                    putExtra(KEY_EXTRA_MONTH, month)
                    putExtra(KEY_EXTRA_DOM, dayOfMonth)
                }

    }

}
