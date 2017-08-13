package jp.toastkid.yobidashi.advertisement

import android.content.Context

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.NativeExpressAdView

import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
internal class TestAdInitializer
/**
 * @param context need ApplicationContext
 */
(context: Context) : AdInitializer {

    init {
        if (!BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        val appAdId = context.getString(R.string.production_app_admob_id)
        MobileAds.initialize(context, appAdId)
    }

    /**
     * Do AdRequest.
     */
    override fun invoke(adView: AdView) {
        if (!BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        adView.loadAd(makeTestAdRequest())
    }

    override fun invoke(interstitialAd: InterstitialAd) {
        if (!BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        interstitialAd.loadAd(makeTestAdRequest())
    }

    override fun invoke(adView: NativeExpressAdView) {
        if (!BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        adView.loadAd(makeTestAdRequest())
    }

    private fun makeTestAdRequest(): AdRequest {
        return AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("59A864957D348217B858A8CE956AA352")
                .build()
    }

}
