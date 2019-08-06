package jp.toastkid.yobidashi.main

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.ScreenMode
import jp.toastkid.yobidashi.browser.archive.ArchivesActivity
import jp.toastkid.yobidashi.browser.bookmark.BookmarkActivity
import jp.toastkid.yobidashi.browser.history.ViewHistoryActivity
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
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.search.favorite.AddingFavoriteSearchService
import jp.toastkid.yobidashi.torch.Torch
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Main of this calendar app.
 *
 * @author toastkidjp
 */
class MainActivity :
        BaseActivity(),
        FragmentReplaceAction,
        ToolbarAction
{

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)

        binding.toolbar.let { toolbar ->
            initToolbar(toolbar)
            setSupportActionBar(toolbar)
            toolbar.setOnClickListener { findCurrentFragment()?.tapHeader() }
        }

        browserFragment = BrowserFragment()

        if (preferenceApplier.useColorFilter()) {
            ColorFilter(this, binding.root).start()
        }

        initializeHeaderViewModel()

        processShortcut(intent)
    }

    private fun initializeHeaderViewModel() {
        val headerViewModel = ViewModelProviders.of(this).get(HeaderViewModel::class.java)
        headerViewModel.title.observe(this, Observer { title ->
            if (title.isNullOrBlank()) {
                return@Observer
            }
            binding.toolbar.title = title
        })
        headerViewModel.url.observe(this, Observer { url ->
            if (url.isNullOrBlank()) {
                return@Observer
            }
            binding.toolbar.subtitle = url
        })
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
            // Add for re-creating activity.
            replaceFragment(browserFragment)
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

        when (preferenceApplier.startUp) {
            StartUp.START -> {
                replaceFragment(homeFragment)
            }
            StartUp.APPS_LAUNCHER -> {
                startActivity(LauncherActivity.makeIntent(this))
                finishWithoutTransition()
            }
            StartUp.BROWSER -> {
                replaceWithBrowser(Uri.EMPTY)
            }
            StartUp.SEARCH -> {
                startActivity(SearchActivity.makeIntent(this))
                finishWithoutTransition()
            }
        }
    }

    private fun finishWithoutTransition() {
        overridePendingTransition(0, 0)
        finish()
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
    private fun replaceFragment(fragment: Fragment) {
        if (fragment.isVisible) {
            return
        }

        val transaction = supportFragmentManager.beginTransaction()
        val fragments = supportFragmentManager?.fragments
        if (fragments?.size != 0) {
            fragments?.get(0)?.let {
                if (it == fragment) {
                    return
                }
                transaction.remove(it)
            }
        }
        transaction.setCustomAnimations(R.anim.slide_in_right, 0, 0, android.R.anim.slide_out_right)
        transaction.add(R.id.content, fragment, fragment::class.java.simpleName)
        transaction.commitAllowingStateLoss()
        binding.toolbar.let {
            if (fragment is CommonFragmentAction) {
                it.setTitle(fragment.titleId())
            }
            it.subtitle = ""
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?) = when (event?.keyCode) {
        KeyEvent.KEYCODE_BACK ->
            findCurrentFragment()?.pressLongBack() ?: super.onKeyLongPress(keyCode, event)
        else ->
            super.onKeyLongPress(keyCode, event)
    }

    override fun onBackPressed() {
        val fragment: CommonFragmentAction? = findCurrentFragment()
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
    private fun findCurrentFragment(): CommonFragmentAction? {
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.content)

        return if (fragment != null) fragment as CommonFragmentAction else null
    }

    /**
     * Show confirm exit.
     */
    private fun confirmExit() {
        CloseDialogFragment()
                .show(supportFragmentManager, CloseDialogFragment::class.java.simpleName)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * Refresh toolbar and background.
     */
    private fun refresh() {
        applyColorToToolbar(binding.toolbar as Toolbar)

        applyBackgrounds()
    }

    /**
     * Apply background appearance.
     */
    private fun applyBackgrounds() {
        val backgroundImagePath = backgroundImagePath
        if (backgroundImagePath.isEmpty()) {
            setBackgroundImage(null)
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
                    binding.root,
                    getString(R.string.message_failed_read_image),
                    colorPair()
            )
            removeBackgroundImagePath()
            setBackgroundImage(null)
        }
    }

    /**
     * Set background image.
     *
     * @param background nullable
     */
    private fun setBackgroundImage(background: BitmapDrawable?) {
        binding.background?.setImageDrawable(background)
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
        when (preferenceApplier.browserScreenMode()) {
            ScreenMode.FIXED -> Unit
            ScreenMode.FULL_SCREEN -> {
                binding.toolbar.visibility = View.GONE
            }
            ScreenMode.EXPANDABLE -> {
                binding.toolbar.animate()?.let {
                    it.cancel()
                    it.translationY(-resources.getDimension(R.dimen.toolbar_height))
                            .setDuration(HEADER_HIDING_DURATION)
                            .withStartAction { binding.content?.requestLayout() }
                            .withEndAction   {
                                binding.toolbar.visibility = View.GONE
                            }
                            .start()
                }
            }
        }
    }

    override fun showToolbar() {
        when (preferenceApplier.browserScreenMode()) {
            ScreenMode.FIXED -> {
                binding.toolbar.visibility = View.VISIBLE
            }
            ScreenMode.FULL_SCREEN -> Unit
            ScreenMode.EXPANDABLE -> {
                binding.toolbar.animate()?.let {
                    it.cancel()
                    it.translationY(0f)
                            .setDuration(HEADER_HIDING_DURATION)
                            .withStartAction {
                                binding.toolbar.visibility = View.VISIBLE
                            }
                            .withEndAction   { binding.content?.requestLayout() }
                            //TODO .start()
                }
            }
        }
    }

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
                            { browserFragment.loadArchive(ArchivesActivity.extractFile(intent)) },
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
        private const val HEADER_HIDING_DURATION = 75L

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_main

        /**
         * For using daily alarm.
         */
        private const val KEY_EXTRA_MONTH = "month"

        /**
         * For using daily alarm.
         */
        private const val KEY_EXTRA_DOM = "dom"

        /**
         * Make launcher intent.
         *
         * @param context
         * @return [Intent]
         */
        fun makeIntent(context: Context) = Intent(context, MainActivity::class.java)
                .also { it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }

        /**
         * Make browser intent.
         *
         * @param context
         * @param uri
         *
         * @return [Intent]
         */
        fun makeBrowserIntent(context: Context, uri: Uri) = Intent(context, MainActivity::class.java)
                .also {
                    it.action = Intent.ACTION_VIEW
                    it.data = uri
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

        /**
         * Make launcher intent.
         *
         * @param context
         * @param dayOfMonth
         * @return [Intent]
         */
        fun makeIntent(context: Context, month: Int, dayOfMonth: Int) = makeIntent(context)
                .also {
                    it.putExtra(KEY_EXTRA_MONTH, month)
                    it.putExtra(KEY_EXTRA_DOM, dayOfMonth)
                }

    }

}
