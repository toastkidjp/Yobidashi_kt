package jp.toastkid.yobidashi.settings.color

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.github.gfx.android.orma.Relation
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater
import jp.toastkid.yobidashi.databinding.ActivitySettingsColorBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitter

/**
 * Color setting activity.
 *
 * @author toastkidjp
 */
class ColorSettingActivity : BaseActivity(), ClearColorsDialogFragment.Callback {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_color)
        binding = DataBindingUtil.setContentView<ActivitySettingsColorBinding>(this, R.layout.activity_settings_color)
        binding!!.activity = this

        val colorPair = colorPair()

        initialBgColor = colorPair.bgColor()
        binding!!.settingsColorPrev.setBackgroundColor(initialBgColor)

        initialFontColor = colorPair.fontColor()
        binding!!.settingsColorPrev.setTextColor(initialFontColor)

        initPalettes()
        initToolbar(binding!!.settingsColorToolbar)
        binding!!.settingsColorToolbar.inflateMenu(R.menu.color_setting_toolbar_menu)
        initSavedColors()
    }

    /**
     * Initialize background and font palettes.
     */
    private fun initPalettes() {
        binding?.backgroundPalette?.addSVBar(binding!!.backgroundSvbar)
        binding?.backgroundPalette?.addOpacityBar(binding!!.backgroundOpacitybar)
        binding?.backgroundPalette?.setOnColorChangedListener ({ c ->
            binding?.settingsColorToolbar?.setBackgroundColor(c)
            binding?.settingsColorOk?.setBackgroundColor(c)
        })

        binding?.fontPalette?.addSVBar(binding!!.fontSvbar)
        binding?.fontPalette?.addOpacityBar(binding!!.fontOpacitybar)
        binding?.fontPalette?.setOnColorChangedListener({ c ->
            binding?.settingsColorToolbar?.setTitleTextColor(c)
            binding?.settingsColorOk?.setTextColor(c)
        })

        refresh()
    }

    /**
     * Initialize saved color's section.
     */
    private fun initSavedColors() {

        adapter = SavedColorAdapter(this, DbInitter.init(this).relationOfSavedColor())
        binding?.savedColors?.adapter = adapter
        binding?.savedColors?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.clearSavedColor?.setOnClickListener{
            ClearColorsDialogFragment().show(
                    supportFragmentManager,
                    ClearColorsDialogFragment::class.java.simpleName
            )
        }
    }

    override fun onClickClearColor() {
        SavedColors.deleteAllAsync(
                binding?.settingsColorToolbar,
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
        holder.textView.setOnClickListener { v -> commitNewColor(color.bgColor, color.fontColor) }
        holder.remove.setOnClickListener { v ->
            adapter!!.removeItemAsMaybe(color)
                    .subscribeOn(Schedulers.io())
                    .subscribe()
            Toaster.snackShort(binding!!.settingsColorToolbar, R.string.settings_color_delete, colorPair())
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
        applyColorToToolbar(binding?.settingsColorToolbar as Toolbar)
        Colors.setColors(binding?.settingsColorOk as TextView, colorPair())
    }

    /**
     * OK button's action.
     *
     * @param ignored defined for Data-Binding
     */
    fun ok(ignored: View) {
        val bgColor = binding!!.backgroundPalette.color
        val fontColor = binding!!.fontPalette.color

        commitNewColor(bgColor, fontColor)

        sendLog(
                "color_set",
                Bundle().apply {
                    putString("bg", Integer.toHexString(bgColor))
                    putString("font", Integer.toHexString(fontColor))
                }
        )

        adapter!!.addItemAsSingle(SavedColors.makeSavedColor(bgColor, fontColor))
                .subscribeOn(Schedulers.io()).subscribe()
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
        Updater.update(this)

        Toaster.snackShort(binding!!.settingsColorToolbar, R.string.settings_color_done_commit, colorPair())
    }

    /**
     * Reset button's action.
     *
     * @param ignored defined for Data-Binding
     */
    fun reset(ignored: View) {
        preferenceApplier.color = initialBgColor

        preferenceApplier.fontColor = initialFontColor

        refresh()
        Updater.update(this)
        Toaster.snackShort(binding!!.settingsColorToolbar, R.string.settings_color_done_reset, colorPair())
    }

    override fun titleId(): Int = R.string.title_settings_color

    override fun clickMenu(item: MenuItem): Boolean {
        if (item.itemId == R.id.color_settings_toolbar_menu_add_recommend) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.title_add_recommended_colors)
                    .setMessage(R.string.message_add_recommended_colors)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok) { d, i ->
                        SavedColors.insertDefaultColors(this)
                        Toaster.snackShort(
                                binding!!.settingsColorToolbar, R.string.done_addition, colorPair())
                        d.dismiss()
                    }
                    .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                    .show()
            return true
        }
        if (item.itemId == R.id.color_settings_toolbar_menu_add_random) {
            SavedColors.insertRandomColors(this)
            Toaster.snackShort(
                    binding!!.settingsColorToolbar, R.string.done_addition, colorPair())
            return true
        }
        return super.clickMenu(item)
    }

    /**
     * Saved color's adapter.
     */
    private inner class SavedColorAdapter(context: Context, relation: Relation<SavedColor, *>)
        : OrmaRecyclerViewAdapter<SavedColor, SavedColorHolder>(context, relation) {

        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ): SavedColorHolder {
            val inflater = LayoutInflater.from(this@ColorSettingActivity)
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
            val intent = Intent(context, ColorSettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }

}
