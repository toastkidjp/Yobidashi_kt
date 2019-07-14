package jp.toastkid.yobidashi.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.ActivitySettingsBinding
import jp.toastkid.yobidashi.libs.Toaster

/**
 * Settings activity.
 *
 * @author toastkidjp
 */
class SettingsActivity : BaseActivity() {

    /**
     * DataBinding object.
     */
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivitySettingsBinding>(this, LAYOUT_ID)
        binding.activity = this
        initToolbar(binding.toolbar)

        supportFragmentManager?.let {
            binding.container.adapter = PagerAdapter(it, { getString(it) })
            binding.container.offscreenPageLimit = 3
        }
    }

    override fun onResume() {
        super.onResume()
        applyColorToToolbar(binding.toolbar)
    }

    @StringRes override fun titleId(): Int = R.string.title_settings

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ColorFilter.REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toaster.snackShort(
                        binding.root,
                        R.string.message_cannot_draw_overlay,
                        colorPair()
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
        private val LAYOUT_ID = R.layout.activity_settings

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
