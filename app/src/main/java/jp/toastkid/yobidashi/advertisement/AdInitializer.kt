package jp.toastkid.yobidashi.advertisement

import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd

/**
 * @author toastkidjp
 */
interface AdInitializer {

    operator fun invoke(adView: AdView)

    operator fun invoke(interstitialAd: InterstitialAd)

}
