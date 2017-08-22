package jp.toastkid.yobidashi.main

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import io.reactivex.disposables.Disposable
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.about.AboutThisAppActivity
import jp.toastkid.yobidashi.advertisement.AdInitializers
import jp.toastkid.yobidashi.barcode.BarcodeReaderActivity
import jp.toastkid.yobidashi.barcode.InstantBarcodeGenerator
import jp.toastkid.yobidashi.browser.BrowserFragment
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
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.planning_poker.PlanningPokerActivity
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchFragment
import jp.toastkid.yobidashi.search.favorite.AddingFavoriteSearchService
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchFragment
import jp.toastkid.yobidashi.search.history.SearchHistoryActivity
import jp.toastkid.yobidashi.settings.SettingsActivity
import java.io.File
import java.io.IOException
import java.text.MessageFormat

/**
 * Main of this calendar app.

 * @author toastkidjp
 */
class MainActivity : BaseActivity(), FragmentReplaceAction {

    /** Navigation's background.  */
    private var navBackground: View? = null

    /** Data binding object.  */
    private lateinit var binding: ActivityMainBinding

    /** Interstitial AD.  */
    private var interstitialAd: InterstitialAd? = null

    /** Calendar.  */
    private var calendarFragment: CalendarFragment? = null

    /** Favorite search.  */
    private var favoriteSearchFragment: FavoriteSearchFragment? = null

    /** Search.  */
    private var searchFragment: SearchFragment? = null

    /** Browser fragment.  */
    private lateinit var browserFragment: BrowserFragment

    /** Home fragment.  */
    private lateinit var homeFragment: HomeFragment

    /** For stop subscribing title pair.  */
    private var prevDisposable: Disposable? = null

    private var adCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, LAYOUT_ID)

        initToolbar(binding.appBarMain.toolbar)
        setSupportActionBar(binding.appBarMain.toolbar)

        initDrawer(binding.appBarMain.toolbar)

        initNavigation()

        setInitialFragment()

        initInterstitialAd()

        if (preferenceApplier.useColorFilter()) {
            ColorFilter(this, binding.root as View).start()
        }

        processShortcut()
    }

    /**
     * Set initial fragment.
     */
    private fun setInitialFragment() {
        homeFragment = HomeFragment()
        replaceFragment(homeFragment)
    }

    /**
     * Process intent shortcut.
     */
    private fun processShortcut() {
        val calledIntent = intent
        if (calledIntent == null || calledIntent.action == null) {
            return
        }

        when (calledIntent.action) {
            Intent.ACTION_VIEW -> {
                loadUri(calledIntent.data)
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

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_VIEW == intent.action) {
            val uri = intent.data
            loadUri(uri)
        }
    }

    private fun loadUri(uri: Uri) {
        if (preferenceApplier.useInternalBrowser()) {
            browserFragment = BrowserFragment()
            val args = Bundle()
            args.putParcelable("url", uri)
            browserFragment.arguments = args
            replaceFragment(browserFragment)
            return
        }
        CustomTabsFactory.make(this, colorPair(), R.drawable.ic_back).build().launchUrl(this, uri)
    }

    /**
     * Replace with passed fragment.
     * @param fragment
     */
    private fun replaceFragment(fragment: BaseFragment) {

        if (prevDisposable != null) {
            prevDisposable?.dispose()
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content, fragment)
        transaction.commit()
        binding.drawerLayout.closeDrawers()
        binding.appBarMain.toolbar.setTitle(fragment.titleId())
        binding.appBarMain.toolbar.subtitle = ""

        if (fragment is BrowserFragment) {
            prevDisposable = fragment.titlePairProcessor()
                    .subscribe { titlePair ->
                        binding.appBarMain.toolbar.title = titlePair.title()
                        binding.appBarMain.toolbar.subtitle = titlePair.subtitle()
                    }
        }

    }

    private fun initInterstitialAd() {
        if (interstitialAd == null) {
            interstitialAd = InterstitialAd(applicationContext)
        }
        if (interstitialAd == null) {
            return
        }
        interstitialAd?.adUnitId = getString(R.string.unit_id_interstitial)
        interstitialAd?.adListener = object : AdListener() {
            val toolbar: Toolbar = binding.appBarMain?.toolbar as Toolbar
            override fun onAdClosed() {
                super.onAdClosed()
                Toaster.snackShort(
                        toolbar,
                        R.string.thank_you_for_using,
                        colorPair()
                )
            }
        }
        AdInitializers.find(this).invoke(interstitialAd as InterstitialAd)
    }

    /**
     * Initialize drawer.
     * @param toolbar
     */
    private fun initDrawer(toolbar: Toolbar) {
        val toggle = object : ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            override fun onDrawerStateChanged(newState: Int) {
                super.onDrawerStateChanged(newState)
                searchFragment?.hideKeyboard()
            }
        }
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    /**
     * Initialize navigation.
     */
    private fun initNavigation() {
        binding.navView.setNavigationItemSelectedListener({ item: MenuItem ->
            attemptToShowingAd()
            when (item.itemId) {
                R.id.nav_search -> {
                    sendLog("nav_search")
                    switchToSearch()
                }
                R.id.nav_search_history -> {
                    sendLog("nav_srch_hstry")
                    startActivity(SearchHistoryActivity.makeIntent(this))
                }
                R.id.nav_calendar -> {
                    sendLog("nav_cal")
                    if (calendarFragment == null) {
                        calendarFragment = CalendarFragment()
                    }
                    replaceFragment(calendarFragment as CalendarFragment)
                }
                R.id.nav_favorite_search -> {
                    sendLog("nav_fav_search")
                    if (favoriteSearchFragment == null) {
                        favoriteSearchFragment = FavoriteSearchFragment()
                    }
                    replaceFragment(favoriteSearchFragment as FavoriteSearchFragment)
                }
                R.id.nav_tweet -> {
                    sendLog("nav_twt")
                    IntentFactory.makeTwitter(
                            this@MainActivity,
                            colorPair(),
                            R.drawable.ic_back
                    ).launchUrl(this@MainActivity, Uri.parse("https://twitter.com/share"))
                }
                R.id.nav_launcher -> {
                    sendLog("nav_lnchr")
                    startActivity(LauncherActivity.makeIntent(this))
                }
                R.id.nav_share -> {
                    sendLog("nav_shr")
                    startActivity(IntentFactory.makeShare(makeShareMessage()))
                }
                R.id.nav_share_twitter -> {
                    sendLog("nav_shr_twt")
                    IntentFactory.makeTwitter(
                            this@MainActivity,
                            colorPair(),
                            R.drawable.ic_back
                    ).launchUrl(
                            this@MainActivity,
                            Uri.parse("https://twitter.com/share?text=" + Uri.encode(makeShareMessage()))
                    )
                }
                R.id.nav_about_this_app -> {
                    sendLog("nav_about")
                    startActivity(AboutThisAppActivity.makeIntent(this))
                }
                R.id.nav_screenshots -> {
                    sendLog("nav_screenshots")
                    startActivity(ScreenshotsActivity.makeIntent(this))
                }
                R.id.nav_google_play -> {
                    sendLog("nav_gplay")
                    startActivity(IntentFactory.googlePlay(BuildConfig.APPLICATION_ID))
                }
                R.id.nav_privacy_policy -> {
                    sendLog("nav_prvcy_plcy")
                    CustomTabsFactory.make(this, colorPair(), R.drawable.ic_back)
                            .build()
                            .launchUrl(this, Uri.parse(getString(R.string.link_privacy_policy)))
                }
                R.id.nav_settings -> {
                    sendLog("nav_set_top")
                    startActivity(SettingsActivity.makeIntent(this))
                }
                R.id.nav_planning_poker -> {
                    sendLog("nav_poker")
                    startActivity(PlanningPokerActivity.makeIntent(this))
                }
                R.id.nav_browser -> {
                    sendLog("nav_browser")
                    loadUri(Uri.parse(preferenceApplier.homeUrl))
                }
                R.id.nav_barcode -> {
                    sendLog("nav_barcode")
                    startActivity(BarcodeReaderActivity.makeIntent(this))
                }
                R.id.nav_instant_barcode -> {
                    sendLog("nav_instant_barcode")
                    InstantBarcodeGenerator(this).invoke()
                }
                R.id.nav_home -> {
                    replaceFragment(homeFragment)
                }
            }
            true
        })
        val headerView = binding.navView?.getHeaderView(0)
        if (headerView != null) {
            navBackground = headerView.findViewById(R.id.nav_header_background)
        }
    }

    private fun switchToSearch() {
        if (searchFragment == null) {
            searchFragment = SearchFragment()
        }
        replaceFragment(searchFragment as SearchFragment)
    }

    override fun onBackPressed() {
        @SuppressLint("RestrictedApi")
        val fragment = supportFragmentManager.fragments[0]
        if (fragment == null) {
            super.onBackPressed()
            return
        }
        fragment as BaseFragment
        if (fragment.pressBack()) {
            return
        }
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
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

    private fun refresh() {
        applyColorToToolbar(binding.appBarMain.toolbar)

        applyBackgrounds()
    }

    private fun attemptToShowingAd() {
        if (interstitialAd!!.isLoaded && 4 <= adCount && preferenceApplier.allowShowingAd()) {
            Toaster.snackShort(
                    binding.appBarMain.toolbar,
                    R.string.message_please_view_ad,
                    colorPair()
            )
            interstitialAd!!.show()
            preferenceApplier.updateLastAd()
        }
        adCount++
    }

    /**
     * Apply background appearance.
     */
    private fun applyBackgrounds() {
        val backgroundImagePath = backgroundImagePath
        val fontColor = colorPair().fontColor()
        if (backgroundImagePath.isEmpty()) {
            setBackgroundImage(null)
            (navBackground?.findViewById(R.id.nav_header_main) as TextView)
                    .setTextColor(fontColor)
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
            e.printStackTrace()
            Toaster.snackShort(
                    navBackground!!,
                    getString(R.string.message_failed_read_image),
                    colorPair()
            )
            removeBackgroundImagePath()
            setBackgroundImage(null)
        }

        (navBackground?.findViewById(R.id.nav_header_main) as TextView).setTextColor(fontColor)
    }

    /**
     * Set background image.
     * @param background nullable
     */
    private fun setBackgroundImage(background: BitmapDrawable?) {
        (navBackground!!.findViewById(R.id.background) as ImageView).setImageDrawable(background)
        binding.appBarMain.background.setImageDrawable(background)
        if (background == null) {
            navBackground?.setBackgroundColor(colorPair().bgColor())
        }
    }

    override fun action(c: Command) {
        when (c) {
            Command.OPEN_BROWSER -> {
                loadUri(Uri.parse(preferenceApplier.homeUrl))
                return
            }
            Command.OPEN_SEARCH -> {
                switchToSearch()
                return
            }
            Command.OPEN_HOME -> {
                replaceFragment(homeFragment)
                return
            }
        }
    }

    /**
     * Make share message.
     * @return string
     */
    private fun makeShareMessage(): String {
        return MessageFormat.format(getString(R.string.message_share), getString(R.string.app_name))
    }

    @StringRes
    override fun titleId(): Int {
        return R.string.app_name
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.activity_main

        /** For using daily alarm.  */
        private val KEY_EXTRA_MONTH = "month"

        /** For using daily alarm.  */
        private val KEY_EXTRA_DOM = "dom"

        /** For using launcher intent.  */
        private val KEY_EXTRA_LAUNCH = "launch"

        /** For using search intent.  */
        private val VALUE_EXTRA_LAUNCH_SEARCH = "search"

        /**
         * Make launcher intent.
         * @param context
         * *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }

        /**
         * Make browser intent.
         * @param context
         * *
         * @return
         */
        fun makeBrowserIntent(context: Context, uri: Uri): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = uri
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }

        /**
         * Make launcher intent.
         * @param context
         * *
         * @return
         */
        fun makeIntent(context: Context, month: Int, dayOfMonth: Int): Intent {
            val intent = makeIntent(context)
            intent.putExtra(KEY_EXTRA_MONTH, month)
            intent.putExtra(KEY_EXTRA_DOM, dayOfMonth)
            return intent
        }


        /**
         * Make launcher intent with search query.
         * @param context
         * *
         * @return launcher intent
         */
        fun makeSearchLauncherIntent(
                context: Context
        ): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra(KEY_EXTRA_LAUNCH, VALUE_EXTRA_LAUNCH_SEARCH)
            return intent
        }

        /**
         * Make launcher intent with search query.
         * @param context
         * *
         * @param query
         * *
         * @return launcher intent
         */
        fun makeSearchIntent(
                context: Context,
                query: String
        ): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            if (query.isNotEmpty()) {
                intent.putExtra(SearchManager.QUERY, query)
            }
            return intent
        }
    }

}
