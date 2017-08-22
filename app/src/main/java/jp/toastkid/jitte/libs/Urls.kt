package jp.toastkid.jitte.libs

import android.text.TextUtils
import android.webkit.URLUtil

/**
 * URL utilities.

 * @author toastkidjp
 */
class Urls private constructor() {

    init {
        throw IllegalStateException()
    }

    companion object {

        /**
         * Return passed url is invalid.
         * @param url
         * *
         * @return
         */
        fun isInvalidUrl(url: String): Boolean {
            return TextUtils.isEmpty(url) || !URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url)
        }

        /**
         * Return passed url is valid.
         * @param url
         * *
         * @return
         */
        fun isValidUrl(url: String): Boolean {
            return !isInvalidUrl(url)
        }
    }
}
