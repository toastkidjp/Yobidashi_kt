package jp.toastkid.yobidashi.settings.color

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.gfx.android.orma.Relation
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater
import jp.toastkid.yobidashi.databinding.ActivitySettingsColorBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitializer
import jp.toastkid.yobidashi.settings.fragment.TitleIdSupplier

/**
 * Color setting activity.
 *
 * @author toastkidjp
 */
class ColorSettingFragment : BaseFragment(), TitleIdSupplier, ClearColorsDialogFragment.Callback {

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
    private var binding: ActivitySettingsColorBinding? = null

    /**
     * Saved color's adapter.
     */
    private var adapter: OrmaRecyclerViewAdapter<SavedColor, SavedColorHolder>? = null

    /**
     * Subscribed disposables.
     */
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.activity_settings_color,
                container,
                false
        )
        binding?.activity = this

        setHasOptionsMenu(true)

        return binding?.root ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorPair = colorPair()

        initPalettes()
        initSavedColors()

        initialBgColor = colorPair.bgColor()
        binding?.settingsColorPrev?.setBackgroundColor(initialBgColor)
        binding?.backgroundPalette?.color = initialBgColor

        initialFontColor = colorPair.fontColor()
        binding?.settingsColorPrev?.setTextColor(initialFontColor)
        binding?.fontPalette?.color = initialFontColor
    }

    /**
     * Initialize background and font palettes.
     */
    private fun initPalettes() {
        binding?.backgroundPalette?.also {
            it.addSVBar(binding?.backgroundSvbar)
            it.addOpacityBar(binding?.backgroundOpacitybar)
            it.setOnColorChangedListener { color ->
                binding?.settingsColorOk?.setBackgroundColor(color)
            }
        }

        binding?.fontPalette?.also {
            it.addSVBar(binding?.fontSvbar)
            it.addOpacityBar(binding?.fontOpacitybar)
            it.setOnColorChangedListener { color ->
                binding?.settingsColorOk?.setTextColor(color)
            }
        }

        refresh()
    }

    /**
     * Initialize saved color's section.
     */
    private fun initSavedColors() {
        val activityContext = context ?: return
        adapter = SavedColorAdapter(
                activityContext,
                DbInitializer.init(activityContext).relationOfSavedColor()
        )
        binding?.savedColors?.adapter = adapter
        binding?.savedColors?.layoutManager =
                LinearLayoutManager(activityContext, LinearLayoutManager.HORIZONTAL, false)
        binding?.clearSavedColor?.setOnClickListener{
            ClearColorsDialogFragment().show(
                    fragmentManager,
                    ClearColorsDialogFragment::class.java.simpleName
            )
        }
    }

    override fun onClickClearColor() {
        SavedColors.deleteAllAsync(
                binding?.root,
                adapter?.relation?.deleter() as? SavedColor_Deleter
        ).addTo(disposables)
    }

    /**
     * Bind value and action to holder's view.
     *
     * @param holder Holder
     * @param color  [SavedColor] object
     */
    private fun bindView(holder: SavedColorHolder, color: SavedColor) {
        SavedColors.setSaved(holder.textView, color)
        holder.textView.setOnClickListener { commitNewColor(color.bgColor, color.fontColor) }
        holder.remove.setOnClickListener {
            adapter?.removeItemAsMaybe(color)
                    ?.subscribeOn(Schedulers.io())
                    ?.subscribe()
                    ?.addTo(disposables)
            snackShort(R.string.settings_color_delete)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * Refresh with current color.
     */
    private fun refresh() {
        Colors.setColors(binding?.settingsColorOk as TextView, colorPair())
    }

    /**
     * OK button's action.
     */
    fun ok() {
        val bgColor = binding?.backgroundPalette?.color ?: Color.BLACK
        val fontColor = binding?.fontPalette?.color ?: Color.WHITE

        commitNewColor(bgColor, fontColor)

        adapter?.addItemAsSingle(SavedColors.makeSavedColor(bgColor, fontColor))
                ?.subscribeOn(Schedulers.io())
                ?.subscribe()
                ?.addTo(disposables)
    }

    /**
     * Commit new color.
     *
     * @param bgColor   Background color int
     * @param fontColor Font color int
     */
    private fun commitNewColor(bgColor: Int, fontColor: Int) {
        val preferenceApplier = preferenceApplier()
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
    fun reset() {
        val preferenceApplier = preferenceApplier()
        preferenceApplier.color = initialBgColor
        preferenceApplier.fontColor = initialFontColor

        refresh()
        activity?.let { Updater.update(it) }
        snackShort(R.string.settings_color_done_reset)
    }

    override fun titleId(): Int = R.string.title_settings_color

    override fun onCreateOptionsMenu(menu: android.view.Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.browser, menu)

        menu?.let { menuNonNull ->
            menuNonNull.findItem(R.id.color_settings_toolbar_menu_add_recommend)
                    ?.setOnMenuItemClickListener {
                        RecommendColorDialogFragment().show(
                                fragmentManager,
                                RecommendColorDialogFragment::class.java.simpleName
                        )
                        true
            }
            menuNonNull.findItem(R.id.color_settings_toolbar_menu_add_random)
                    ?.setOnMenuItemClickListener {
                        val activityContext = context ?: return@setOnMenuItemClickListener true
                        SavedColors.insertRandomColors(activityContext).addTo(disposables)
                        snackShort(R.string.done_addition)
                        true
                    }
        }
    }

    private fun snackShort(@StringRes messageId: Int) {
        binding?.root?.let {
            Toaster.snackShort(it, messageId, colorPair())
        }
    }

    /**
     * Saved color's adapter.
     */
    private inner class SavedColorAdapter(activityContext: Context, relation: Relation<SavedColor, *>)
        : OrmaRecyclerViewAdapter<SavedColor, SavedColorHolder>(activityContext, relation) {

        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ): SavedColorHolder {
            val inflater = LayoutInflater.from(context)
            return SavedColorHolder(inflater.inflate(R.layout.saved_color, parent, false))
        }

        override fun onBindViewHolder(holder: SavedColorHolder, position: Int) {
            bindView(holder, relation.get(position))
        }

        override fun getItemCount(): Int = relation.count()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    companion object {

        /**
         * Make launcher intent.
         *
         * @param context Context
         *
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, ColorSettingFragment::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }

}
