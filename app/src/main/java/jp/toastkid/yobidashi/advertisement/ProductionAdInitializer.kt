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
internal class ProductionAdInitializer
/**
 * @param context need ApplicationContext
 */
(context: Context) : AdInitializer {

    init {
        if (BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        val appAdId = context.getString(R.string.production_app_admob_id)
        MobileAds.initialize(context, appAdId)
    }

    /**

     */
    override fun invoke(adView: AdView) {
        if (BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        adView.loadAd(makeRequest())
    }

    override fun invoke(interstitialAd: InterstitialAd) {
        if (BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        interstitialAd.loadAd(makeRequest())
    }

    override fun invoke(interstitialAd: NativeExpressAdView) {
        if (BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        interstitialAd.loadAd(makeRequest())
    }

    private fun makeRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}
