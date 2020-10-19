package jp.toastkid.yobidashi.settings.color

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater
import jp.toastkid.yobidashi.databinding.FragmentSettingsColorBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.settings.fragment.TitleIdSupplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Color setting activity.
 *
 * TODO clean up with ViewModel.
 *
 * @author toastkidjp
 */
class ColorSettingFragment : Fragment(),
        CommonFragmentAction,
        ClearColorsDialogFragment.Callback {

    /**
     * Initial background color.
     */
    private var initialBgColor: Int = 0

    /**
     * Initial font color.
     */
    private var initialFontColor: Int = 0

    /**
     * Data-Binding object.
     */
    private var binding: FragmentSettingsColorBinding? = null

    /**
     * Saved color's adapter.
     */
    private var adapter: SavedColorAdapter? = null

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var repository: SavedColorRepository

    /**
     * Subscribed disposables.
     */
    private val disposables: Job by lazy { Job() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
                inflater,
                LAYOUT_ID,
                container,
                false
        )
        binding?.fragment = this

        val context = context ?: return null
        preferenceApplier = PreferenceApplier(context)

        setHasOptionsMenu(true)

        return binding?.root ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorPair = colorPair()

        initPalettes()
        initSavedColors()

        initialBgColor = colorPair.bgColor()
        binding?.prev?.setBackgroundColor(initialBgColor)
        binding?.backgroundPalette?.color = initialBgColor

        initialFontColor = colorPair.fontColor()
        binding?.prev?.setTextColor(initialFontColor)
        binding?.fontPalette?.color = initialFontColor

        binding?.ok?.setOnClickListener { ok() }
        binding?.prev?.setOnClickListener { reset() }
    }

    /**
     * Initialize background and font palettes.
     */
    private fun initPalettes() {
        binding?.backgroundPalette?.also {
            it.addSVBar(binding?.backgroundSvbar)
            it.addOpacityBar(binding?.backgroundOpacitybar)
            it.setOnColorChangedListener { color ->
                binding?.ok?.setBackgroundColor(color)
            }
        }

        binding?.fontPalette?.also {
            it.addSVBar(binding?.fontSvbar)
            it.addOpacityBar(binding?.fontOpacitybar)
            it.setOnColorChangedListener { color ->
                binding?.ok?.setTextColor(color)
            }
        }

        refresh()
    }

    /**
     * Initialize saved color's section.
     */
    private fun initSavedColors() {
        val activityContext = context ?: return

        repository = DatabaseFinder().invoke(activityContext).savedColorRepository()

        adapter = SavedColorAdapter(
                LayoutInflater.from(activityContext),
                repository,
                ViewModelProvider(requireActivity()).get(ContentViewModel::class.java),
                this::commitNewColor
        )

        binding?.savedColors?.adapter = adapter
        binding?.savedColors?.layoutManager =
                GridLayoutManager(activityContext, 3, LinearLayoutManager.VERTICAL, false)
        binding?.clearSavedColor?.setOnClickListener{
            val clearColorsDialogFragment = ClearColorsDialogFragment()
            clearColorsDialogFragment.setTargetFragment(this, 1)
            clearColorsDialogFragment.show(
                    parentFragmentManager,
                    ClearColorsDialogFragment::class.java.simpleName
            )
        }
    }

    override fun onClickClearColor() {
        adapter?.clear()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * Refresh with current color.
     */
    private fun refresh() {
        binding?.ok?.also { colorPair().setTo(it) }
        adapter?.refresh()
    }

    /**
     * OK button's action.
     */
    private fun ok() {
        val bgColor = binding?.backgroundPalette?.color ?: Color.BLACK
        val fontColor = binding?.fontPalette?.color ?: Color.WHITE

        commitNewColor(bgColor, fontColor)

        CoroutineScope(Dispatchers.Main).launch(disposables) {
            withContext(Dispatchers.IO) {
                val savedColor = SavedColor.make(bgColor, fontColor)
                repository.add(savedColor)
                adapter?.add(savedColor)
            }

            adapter?.notifyDataSetChanged()
        }
    }

    /**
     * Commit new color.
     *
     * @param bgColor   Background color int
     * @param fontColor Font color int
     */
    private fun commitNewColor(bgColor: Int, fontColor: Int) {
        preferenceApplier.color = bgColor
        preferenceApplier.fontColor = fontColor

        refresh()

        binding?.backgroundPalette?.color = bgColor
        binding?.fontPalette?.color = fontColor
        activity?.let { Updater.update(it) }

        snackShort(R.string.settings_color_done_commit)
    }

    /**
     * Reset button's action.
     */
    private fun reset() {
        preferenceApplier.color = initialBgColor
        preferenceApplier.fontColor = initialFontColor

        refresh()
        activity?.let { Updater.update(it) }
        snackShort(R.string.settings_color_done_reset)
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.color_setting_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.color_settings_toolbar_menu_add_recommend -> {
                RecommendColorDialogFragment().show(
                        parentFragmentManager,
                        RecommendColorDialogFragment::class.java.simpleName
                )
                true
            }
            R.id.color_settings_toolbar_menu_add_random -> {
                RandomColorInsertion(repository)() {
                    adapter?.refresh()
                }
                snackShort(R.string.done_addition)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun snackShort(@StringRes messageId: Int) {
        binding?.root?.let {
            Toaster.snackShort(it, messageId, colorPair())
        }
    }

    private fun colorPair() = preferenceApplier.colorPair()

    override fun onDetach() {
        disposables.cancel()
        super.onDetach()
    }

    companion object : TitleIdSupplier {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_settings_color

        override fun titleId(): Int = R.string.title_settings_color

    }
}
