package jp.toastkid.yobidashi.about

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.NativeExpressAdView

import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.advertisement.AdInitializer
import jp.toastkid.yobidashi.advertisement.AdInitializers
import jp.toastkid.yobidashi.advertisement.NativeAdFactory
import jp.toastkid.yobidashi.databinding.ActivityAboutBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.LicenseViewer
import jp.toastkid.yobidashi.libs.Toaster

/**
 * About this app.

 * @author toastkidjp
 */
class AboutThisAppActivity : BaseActivity() {

    /** Native AD view.  */
    private var nativeAd: NativeExpressAdView? = null

    /** Data Binding.  */
    private var binding: ActivityAboutBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView<ActivityAboutBinding>(this, LAYOUT_ID)
        binding!!.activity = this

        initToolbar(binding!!.toolbar)

        binding!!.settingsAppVersion.text = BuildConfig.VERSION_NAME

        val appContext = applicationContext

        val adInitializer = AdInitializers.find(appContext)
        nativeAd = NativeAdFactory.make(appContext)
        nativeAd!!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Toaster.snackShort(
                        nativeAd!!,
                        R.string.message_done_load_ad,
                        colorPair()
                )
            }

            override fun onAdFailedToLoad(i: Int) {
                super.onAdFailedToLoad(i)
                Toaster.snackShort(
                        nativeAd!!,
                        R.string.message_failed_ad_loading,
                        colorPair()
                )
            }
        }
        binding!!.ad.addView(nativeAd)
        adInitializer.invoke(nativeAd!!)
    }

    override fun onResume() {
        super.onResume()

        applyColorToToolbar(binding!!.toolbar)
        ImageLoader.setImageToImageView(binding!!.backgroundImage, backgroundImagePath)
    }

    /**
     * Show licenses dialog.
     * @param view
     */
    fun licenses(view: View) {
        LicenseViewer(this).invoke()
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAd!!.destroy()
    }

    override fun titleId(): Int {
        return R.string.title_about_this_app
    }

    companion object {

        /** Layout ID.  */
        private val LAYOUT_ID = R.layout.activity_about

        /**
         * Make launcher intent.
         * @param context
         * *
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, AboutThisAppActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
