package jp.toastkid.yobidashi.advertisement

import android.content.Context
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import jp.toastkid.yobidashi.R

/**
 * Banner AD factory.
 *
 * @author toastkidjp
 */
object BannerAdFactory {

    /**
     * Make banner ad view.
     *
     * @param context Context
     * @return [AdView]
     */
    fun make(context: Context): AdView {
        val adView = AdView(context)
        adView.adUnitId = context.getString(R.string.unit_id_banner_ad)
        adView.adSize = AdSize.BANNER
        return adView
    }
}
