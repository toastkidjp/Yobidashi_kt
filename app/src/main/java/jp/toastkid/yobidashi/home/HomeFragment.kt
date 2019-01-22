package jp.toastkid.yobidashi.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.barcode.BarcodeReaderActivity
import jp.toastkid.yobidashi.databinding.FragmentHomeBinding
import jp.toastkid.yobidashi.launcher.LauncherActivity
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.main.ToolbarAction
import jp.toastkid.yobidashi.planning_poker.PlanningPokerActivity
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingsActivity
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity
import jp.toastkid.yobidashi.settings.color.ColorSettingActivity
import timber.log.Timber

/**
 * Home fragment.
 *
 * @author toastkidjp
 */
class HomeFragment : BaseFragment() {

    /**
     * Data binding object.
     */
    private lateinit var binding: FragmentHomeBinding

    /**
     * Callback.
     */
    private var action: FragmentReplaceAction? = null

    /**
     * For hiding toolbar.
     */
    private var toolbarAction: ToolbarAction? = null

    /**
     * ModuleAdapter.
     */
    private var adapter: Adapter? = null

    /**
     * Disposables.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        action = context as FragmentReplaceAction?
        toolbarAction = context as ToolbarAction?
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this

        initMenus()

        return binding.root
    }

    /**
     * Initialize RecyclerView menu.
     */
    private fun initMenus() {
        val fragmentActivity = activity ?: return
        adapter = Adapter(fragmentActivity, Consumer { this.processMenu(it) })
        binding.menusView.adapter = adapter
        val layoutManager = LinearLayoutManager(fragmentActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.menusView.layoutManager = layoutManager
        layoutManager.scrollToPosition(Adapter.mediumPosition())
    }

    /**
     * Process menu.
     * @param menu
     */
    private fun processMenu(menu: Menu) {
        val fragmentActivity = activity ?: return
        when (menu) {
            Menu.CODE_READER -> {
                startActivity(BarcodeReaderActivity.makeIntent(fragmentActivity))
            }
            Menu.LAUNCHER -> {
                startActivity(LauncherActivity.makeIntent(fragmentActivity))
            }
            Menu.BROWSER -> {
                action?.action(Command.OPEN_BROWSER)
            }
            Menu.PLANNING_POKER -> {
                startActivity(PlanningPokerActivity.makeIntent(fragmentActivity))
            }
            Menu.SETTING -> {
                startActivity(SettingsActivity.makeIntent(fragmentActivity))
            }
            Menu.COLOR_SETTING -> {
                startActivity(ColorSettingActivity.makeIntent(fragmentActivity))
            }
            Menu.BACKGROUND_SETTING -> {
                startActivity(BackgroundSettingActivity.makeIntent(fragmentActivity))
            }
            Menu.WIFI_SETTING -> {
                startActivity(SettingsIntentFactory.wifi())
            }
            Menu.EXIT -> {
                fragmentActivity.moveTaskToBack(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.root.setBackgroundColor(
                if (preferenceApplier().hasBackgroundImagePath())
                    Color.TRANSPARENT
                else
                    activity?.let { ContextCompat.getColor(it, R.color.darkgray_scale) } ?: Color.TRANSPARENT
        )

        val colorPair = colorPair()
        @ColorInt val fontColor: Int = colorPair.fontColor()

        binding.mainTitle.setTextColor(colorPair().fontColor())
        binding.searchAction.setTextColor(fontColor)

        @ColorInt val bgColor:   Int = colorPair.bgColor()
        binding.searchInput.setTextColor(bgColor)
        binding.searchActionBackground.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 128))
        binding.searchIcon.setColorFilter(bgColor)
        binding.voiceSearch.setColorFilter(bgColor)
        binding.searchInputBorder.setBackgroundColor(bgColor)

        binding.menusView.requestLayout()

        toolbarAction?.hideToolbar()
    }

    /**
     * Open search.
     */
    fun search() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.searchBar.transitionName = "share"
        }

        action?.action(Command.OPEN_SEARCH)
    }

    /**
     * Open voice search.
     */
    fun voiceSearch() {
        activity?.let {
            try {
                startActivityForResult(VoiceSearch.makeIntent(it), VoiceSearch.REQUEST_CODE)
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
                VoiceSearch.suggestInstallGoogleApp(binding.root, colorPair())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        when (requestCode) {
            VoiceSearch.REQUEST_CODE -> {
                activity?.let {
                    VoiceSearch.processResult(it, data).addTo(disposables)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        adapter?.dispose()
        disposables.clear()
    }

    @StringRes
    override fun titleId(): Int = R.string.title_home

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_home

    }
}
