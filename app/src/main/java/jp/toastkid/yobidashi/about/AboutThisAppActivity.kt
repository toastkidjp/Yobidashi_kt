package jp.toastkid.yobidashi.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityAboutBinding
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier

/**
 * About this app.
 *
 * @author toastkidjp
 */
class AboutThisAppActivity : AppCompatActivity() {

    /**
     * Data Binding.
     */
    private var binding: ActivityAboutBinding? = null

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        binding?.activity = this

        binding?.toolbar?.also { toolbar ->
            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.setTitle(R.string.title_about_this_app)
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }
        }

        binding?.settingsAppVersion?.text = BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()

        val colorPair = preferenceApplier.colorPair()
        binding?.appBar?.setBackgroundColor(colorPair.bgColor())
        binding?.toolbar?.let { ToolbarColorApplier()(window, it, colorPair) }
    }

    /**
     * Show licenses dialog.
     * @param view
     */
    fun licenses(view: View) {
        val intent = Intent(this, OssLicensesMenuActivity::class.java)
        OssLicensesMenuActivity.setActivityTitle(view.context.getString(R.string.title_licenses))
        startActivity(intent)
    }

    fun checkUpdate() {
        startActivity(IntentFactory.googlePlay(BuildConfig.APPLICATION_ID))
    }

    fun privacyPolicy() {
        CustomTabsFactory.make(this, preferenceApplier.colorPair())
                .build()
                .launchUrl(this, Uri.parse(getString(R.string.link_privacy_policy)))
    }

    fun aboutAuthorApp() {
        startActivity(IntentFactory.authorsApp())
    }

    fun clickMenu(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_exit -> moveTaskToBack(true)
            R.id.menu_close -> finish()
        }
        return true
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_about

        /**
         * Make launcher intent.
         * @param context
         *
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, AboutThisAppActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
