package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class AlphaConverter {

    @ColorInt
    fun readBackground(context: Context) =
            ColorUtils.setAlphaComponent(
                    Color.WHITE,
                    Math.round((255f * PreferenceApplier(context).getWebViewBackgroundAlpha()))
            )
}
