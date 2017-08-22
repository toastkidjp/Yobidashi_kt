package jp.toastkid.jitte.advertisement

import android.content.Context

import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.NativeExpressAdView
import com.google.android.gms.ads.VideoOptions

import jp.toastkid.jitte.R

/**
 * Native AD factory.

 * @author toastkidjp
 */
object NativeAdFactory {

    /** Video Options.  */
    private val VIDEO_OPTIONS = VideoOptions.Builder().setStartMuted(true).build()

    /**
     * Make native ad view.
     * @param appContext Application context
     * *
     * @return [NativeExpressAdView]
     */
    fun make(appContext: Context): NativeExpressAdView {
        val nAd = NativeExpressAdView(appContext)
        nAd.adUnitId = appContext.getString(R.string.unit_id_native_large_ad)
        nAd.adSize = AdSize.MEDIUM_RECTANGLE
        nAd.videoOptions = VIDEO_OPTIONS
        return nAd
    }
}
