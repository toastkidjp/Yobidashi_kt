package jp.toastkid.jitte.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import jp.toastkid.jitte.BaseFragment
import jp.toastkid.jitte.R
import jp.toastkid.jitte.barcode.BarcodeReaderActivity
import jp.toastkid.jitte.barcode.InstantBarcodeGenerator
import jp.toastkid.jitte.color_filter.ColorFilter
import jp.toastkid.jitte.databinding.FragmentHomeBinding
import jp.toastkid.jitte.launcher.LauncherActivity
import jp.toastkid.jitte.libs.Toaster
import jp.toastkid.jitte.libs.intent.SettingsIntentFactory
import jp.toastkid.jitte.planning_poker.PlanningPokerActivity
import jp.toastkid.jitte.search.voice.VoiceSearch
import jp.toastkid.jitte.settings.SettingsActivity
import jp.toastkid.jitte.settings.background.BackgroundSettingActivity
import jp.toastkid.jitte.settings.color.ColorSettingActivity

/**
 * Speed dial.
 *
 * @author toastkidjp
 */
class HomeFragment : BaseFragment() {

    /** Data binding object.  */
    private lateinit var binding: FragmentHomeBinding

    /** Callback.  */
    private var action: FragmentReplaceAction? = null

    /** ModuleAdapter.  */
    private var adapter: Adapter? = null

    /** Disposables.  */
    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        action = context as FragmentReplaceAction?
    }

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater!!, LAYOUT_ID, container, false)
        binding.fragment = this

        initMenus()

        return binding.root
    }

    /**
     * Initialize RecyclerView menu.
     */
    private fun initMenus() {
        adapter = Adapter(activity, Consumer<Menu> { this.processMenu(it) })
        binding.menusView.adapter = adapter
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.menusView.layoutManager = layoutManager
        layoutManager.scrollToPosition(Adapter.mediumPosition())
    }

    /**
     * Process menu.
     * @param menu
     */
    private fun processMenu(menu: Menu) {
        when (menu) {
            Menu.CODE_READER -> {
                startActivity(BarcodeReaderActivity.makeIntent(activity))
                return
            }
            Menu.SHARE_BARCODE -> {
                InstantBarcodeGenerator(activity).invoke()
                return
            }
            Menu.LAUNCHER -> {
                startActivity(LauncherActivity.makeIntent(activity))
                return
            }
            Menu.BROWSER -> {
                action!!.action(Command.OPEN_BROWSER)
                return
            }
            Menu.PLANNING_POKER -> {
                startActivity(PlanningPokerActivity.makeIntent(activity))
                return
            }
            Menu.SETTING -> {
                startActivity(SettingsActivity.makeIntent(activity))
                return
            }
            Menu.COLOR_SETTING -> {
                startActivity(ColorSettingActivity.makeIntent(activity))
                return
            }
            Menu.BACKGROUND_SETTING -> {
                startActivity(BackgroundSettingActivity.makeIntent(activity))
                return
            }
            Menu.WIFI_SETTING -> {
                startActivity(SettingsIntentFactory.wifi())
                return
            }
            Menu.COLOR_FILTER -> {
                ColorFilter(activity, binding.root)
                        .switchState(this, REQUEST_OVERLAY_PERMISSION)
                return
            }
            Menu.EXIT -> {
                activity.finish()
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.root.setBackgroundColor(
                if (preferenceApplier().hasBackgroundImagePath())
                    Color.TRANSPARENT
                else
                    ContextCompat.getColor(activity, R.color.darkgray_scale)
        )
        val colorPair = colorPair()
        @ColorInt val bgColor = colorPair.bgColor()
        @ColorInt val fontColor = colorPair.fontColor()
        binding.mainTitle.setTextColor(colorPair().fontColor())
        binding.searchInput.setTextColor(bgColor)
        binding.searchActionBackground.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 128))
        binding.searchAction.setTextColor(fontColor)
        binding.searchIcon.setColorFilter(bgColor)
        binding.voiceSearch.setColorFilter(bgColor)
        binding.searchInputBorder.setBackgroundColor(bgColor)
    }

    /**
     * Open search.
     * @param v
     */
    fun search(v: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.searchBar.transitionName = "share"
        }

        action!!.action(Command.OPEN_SEARCH)
    }

    /**
     * Open voice search.
     * @param v
     */
    fun voiceSearch(v: View) {
        startActivityForResult(VoiceSearch.makeIntent(activity), REQUEST_CODE_VOICE_SEARCH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        when (requestCode) {
            REQUEST_CODE_VOICE_SEARCH -> {
                disposables.add(VoiceSearch.processResult(activity, data))
                return
            }
            REQUEST_OVERLAY_PERMISSION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                    Toaster.snackShort(
                            binding.root,
                            R.string.message_cannot_draw_overlay,
                            colorPair()
                    )
                    return
                }
                ColorFilter(activity, binding.root)
                        .switchState(this, REQUEST_OVERLAY_PERMISSION)
                return
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        adapter?.dispose()
        disposables.dispose()
    }

    override fun titleId(): Int {
        return R.string.title_home
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.fragment_home

        private val REQUEST_CODE_VOICE_SEARCH = 2

        private val REQUEST_OVERLAY_PERMISSION = 3
    }
}
