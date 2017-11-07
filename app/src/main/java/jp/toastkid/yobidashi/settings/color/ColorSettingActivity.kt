package jp.toastkid.yobidashi.settings.color

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.github.gfx.android.orma.Relation
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter

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

 * @author toastkidjp
 */
class ColorSettingActivity : BaseActivity() {

    private var initialBgColor: Int = 0

    private var initialFontColor: Int = 0

    private var binding: ActivitySettingsColorBinding? = null

    private var adapter: OrmaRecyclerViewAdapter<SavedColor, SavedColorHolder>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_color)
        binding = DataBindingUtil.setContentView<ActivitySettingsColorBinding>(this, R.layout.activity_settings_color)
        binding!!.activity = this

        val colorPair = colorPair()

        initialBgColor = colorPair.bgColor()
        binding!!.settingsColorPrev.setBackgroundColor(initialBgColor)

        initialFontColor = colorPair.fontColor()
        binding!!.settingsColorPrev.setTextColor(initialFontColor)

        initPalette()
        initToolbar(binding!!.settingsColorToolbar)
        binding!!.settingsColorToolbar.inflateMenu(R.menu.color_setting_toolbar_menu)
        initSavedColors()
    }

    private fun initPalette() {
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

    private fun initSavedColors() {

        adapter = SavedColorAdapter(this, DbInitter.init(this).relationOfSavedColor())
        binding!!.savedColors.adapter = adapter
        binding!!.savedColors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding!!.clearSavedColor.setOnClickListener({ v ->
            SavedColors.showClearColorsDialog(
                    this,
                    binding!!.settingsColorToolbar,
                    adapter!!.relation as SavedColor_Relation
            )
        })
    }

    /**
     * Bind value and action to holder's view.
     * @param holder Holder
     *
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

    private fun refresh() {
        applyColorToToolbar(binding!!.settingsColorToolbar)
        Colors.setBgAndText(binding!!.settingsColorOk, colorPair())
    }

    fun ok(view: View) {
        val bgColor = binding!!.backgroundPalette.color
        val fontColor = binding!!.fontPalette.color

        commitNewColor(bgColor, fontColor)

        val bundle = Bundle()
        bundle.putString("bg", Integer.toHexString(bgColor))
        bundle.putString("font", Integer.toHexString(fontColor))
        sendLog("color_set", bundle)

        adapter!!.addItemAsSingle(SavedColors.makeSavedColor(bgColor, fontColor))
                .subscribeOn(Schedulers.io()).subscribe()
    }

    private fun commitNewColor(bgColor: Int, fontColor: Int) {
        preferenceApplier!!.color = bgColor

        preferenceApplier!!.fontColor = fontColor

        refresh()

        binding!!.backgroundPalette.color = bgColor
        binding!!.fontPalette.color = fontColor
        Updater.update(this)

        Toaster.snackShort(binding!!.settingsColorToolbar, R.string.settings_color_done_commit, colorPair())
    }

    fun reset(view: View) {
        preferenceApplier!!.color = initialBgColor

        preferenceApplier!!.fontColor = initialFontColor

        refresh()
        Updater.update(this)
        Toaster.snackShort(binding!!.settingsColorToolbar, R.string.settings_color_done_reset, colorPair())
    }

    override fun titleId(): Int {
        return R.string.title_settings_color
    }

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

    private inner class SavedColorAdapter(context: Context, relation: Relation<SavedColor, *>) : OrmaRecyclerViewAdapter<SavedColor, SavedColorHolder>(context, relation) {

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

        override fun getItemCount(): Int {
            return relation.count()
        }
    }

    companion object {

        /**
         * Make launcher intent.
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
