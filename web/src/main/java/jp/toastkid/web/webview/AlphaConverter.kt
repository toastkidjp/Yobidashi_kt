package jp.toastkid.web.webview

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import jp.toastkid.lib.preference.PreferenceApplier
import kotlin.math.roundToInt

/**
 * Read WebView's background alpha color from shared preferences.
 *
 * @author toastkidjp
 */
class AlphaConverter {

    @ColorInt
    fun readBackground(context: Context): Int {
        val preferenceApplier = PreferenceApplier(context)
        return ColorUtils.setAlphaComponent(
            if (preferenceApplier.useDarkMode()) Color.BLACK else Color.WHITE,
            (255f * preferenceApplier.getWebViewBackgroundAlpha()).roundToInt()
        )
    }
}
