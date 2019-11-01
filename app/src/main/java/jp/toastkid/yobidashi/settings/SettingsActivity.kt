package jp.toastkid.yobidashi.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
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
            toolbar.setTitle(titleId())
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }
        }

        supportFragmentManager?.let {
            binding.container.adapter = PagerAdapter(it, { getString(it) })
            binding.container.offscreenPageLimit = 3
        }
    }

    override fun onResume() {
        super.onResume()
        ToolbarColorApplier()(window, binding.toolbar, preferenceApplier.colorPair())
    }

    private fun clickMenu(item: MenuItem) = when (item.itemId) {
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

    @StringRes private fun titleId(): Int = R.string.title_settings

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
        private const val LAYOUT_ID = R.layout.activity_settings

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
