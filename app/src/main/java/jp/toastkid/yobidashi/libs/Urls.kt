package jp.toastkid.yobidashi.libs

import android.text.TextUtils
import android.webkit.URLUtil

/**
 * URL utilities.
 *
 * @author toastkidjp
 */
object Urls {

    /**
     * Return passed url is invalid.
     * @param url
     *
     * @return
     */
    fun isInvalidUrl(url: String): Boolean =
            TextUtils.isEmpty(url) || !URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url)

    /**
     * Return passed url is valid.
     * @param url
     *
     * @return
     */
    fun isValidUrl(url: String): Boolean = !isInvalidUrl(url)
}
