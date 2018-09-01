package jp.toastkid.yobidashi.libs

import android.os.Build
import android.text.Html
import android.text.Spanned

/**
 * For using Html.fromHtml without lint warnings.
 *
 * @author toastkidjp
 */
object HtmlCompat {

    /**
     * Implements for only avoiding lint warning.
     *
     * @param html string of html.
     */
    fun fromHtml(html: String?): Spanned? {
        return when {
            html == null -> null
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.M -> {
                @Suppress("DEPRECATION")
                Html.fromHtml(html)
            }
            Build.VERSION.SDK_INT > Build.VERSION_CODES.M -> {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            }
            else -> null
        }
    }
}