package jp.toastkid.yobidashi.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.ActivitySettingsBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier

/**
 * Settings activity.
 *
 * @author toastkidjp
 */
class SettingsActivity : AppCompatActivity() {

    /**
     * DataBinding object.
     */
    private lateinit var binding: ActivitySettingsBinding

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        binding.activity = this
        binding.toolbar.also { toolbar ->
            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.setTitle(TITLE_ID)
            setSupportActionBar(toolbar)
        }

        supportFragmentManager?.let { fragmentManager ->
            binding.container.adapter = PagerAdapter(fragmentManager) { getString(it) }
            binding.container.offscreenPageLimit = 3
        }
    }

    override fun onResume() {
        super.onResume()

        val colorPair = preferenceApplier.colorPair()
        ToolbarColorApplier()(window, binding.toolbar, colorPair)
        binding.tabStrip.also {
            it.setBackgroundColor(colorPair.bgColor())
            it.setTextColor(colorPair.fontColor())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.settings_toolbar_menu, menu)
        menuInflater.inflate(R.menu.setting_tab_shortcut, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.menu_search -> {
            binding.container.currentItem = 2
            true
        }
        R.id.menu_browser -> {
            binding.container.currentItem = 3
            true
        }
        R.id.menu_editor -> {
            binding.container.currentItem = 4
            true
        }
        R.id.menu_notification -> {
            binding.container.currentItem = 6
            true
        }
        R.id.menu_other -> {
            binding.container.currentItem = 7
            true
        }
        R.id.menu_exit -> {
            moveTaskToBack(true)
            true
        }
        R.id.menu_close -> {
            finish()
            true
        }
        else -> true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ColorFilter.REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toaster.snackShort(
                        binding.root,
                        R.string.message_cannot_draw_overlay,
                        preferenceApplier.colorPair()
                )
                return
            }
            ColorFilter(this, binding.root).start()
        }
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_settings

        @StringRes
        private const val TITLE_ID = R.string.title_settings

        /**
         * Make this activity's intent.
         * @param context
         *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
