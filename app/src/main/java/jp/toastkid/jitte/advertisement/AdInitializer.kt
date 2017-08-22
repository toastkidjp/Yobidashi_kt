package jp.toastkid.jitte.advertisement

import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.NativeExpressAdView

/**
 * @author toastkidjp
 */
interface AdInitializer {
    operator fun invoke(adView: AdView)

    operator fun invoke(interstitialAd: InterstitialAd)

    operator fun invoke(adView: NativeExpressAdView)
}
