package jp.toastkid.yobidashi.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityAboutBinding
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.intent.IntentFactory

/**
 * About this app.
 *
 * @author toastkidjp
 */
class AboutThisAppActivity : BaseActivity() {

    /**
     * Data Binding.
     */
    private var binding: ActivityAboutBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        binding?.activity = this

        binding?.toolbar?.let { initToolbar(it) }

        binding?.settingsAppVersion?.text = BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()

        binding?.appBar?.setBackgroundColor(colorPair().bgColor())
        binding?.toolbar?.let { applyColorToToolbar(it) }
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
        CustomTabsFactory.make(this, colorPair())
                .build()
                .launchUrl(this, Uri.parse(getString(R.string.link_privacy_policy)))
    }

    fun aboutAuthorApp() {
        startActivity(IntentFactory.authorsApp())
    }

    override fun titleId(): Int = R.string.title_about_this_app

    companion object {

        /**
         * Layout ID.
         */
        private val LAYOUT_ID = R.layout.activity_about

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
