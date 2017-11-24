package jp.toastkid.yobidashi.advertisement

import android.content.Context
import com.google.android.gms.ads.*
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R

/**
 * For test environment AD initializer.
 *
 * @param context need ApplicationContext
 * @author toastkidjp
 */
internal class TestAdInitializer(context: Context) : AdInitializer {

    init {
        if (!BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        val appAdId = context.getString(R.string.production_app_admob_id)
        MobileAds.initialize(context, appAdId)
    }

    /**
     * Do AdRequest with [AdView].
     *
     * @param adView [AdView]
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

    /**
     * Make test [AdRequest] instance.
     *
     * @return [AdRequest] instance
     */
    private fun makeTestAdRequest(): AdRequest {
        return AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("59A864957D348217B858A8CE956AA352")
                .addTestDevice("41D3185792903C624B6E9045EBF43BB3")
                .addTestDevice("FF30448442F5EAE65974D6E0FEB4C1BD")
                .build()
    }

}
