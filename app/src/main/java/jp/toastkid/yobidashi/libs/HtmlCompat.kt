package jp.toastkid.yobidashi.libs

import android.os.Build
import android.text.Html
import android.text.Spanned

/**
 * @author toastkidjp
 */
object HtmlCompat {

    fun fromHtml(html: String?): Spanned? {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        } else {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        }
    }
}