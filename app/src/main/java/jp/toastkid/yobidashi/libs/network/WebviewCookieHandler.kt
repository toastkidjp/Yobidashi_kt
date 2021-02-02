package jp.toastkid.yobidashi.libs.network

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * For sharing [WebView]'s cookie.
 *
 * @author toastkidjp
 */
object WebViewCookieHandler : CookieJar {

    private const val DELIMITER = ";"

    /**
     * Use for extract [WebView]'s cookies.
     */
    private val cookieManager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url?.toString() ?: return
        cookies?.forEach { cookieManager.setCookie(urlString, it.toString()) }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val urlString = url?.toString() ?: return emptyList()
        val cookiesString = cookieManager.getCookie(urlString)

        return if (cookiesString != null && cookiesString.isNotEmpty()) {
            cookiesString
                    .split(DELIMITER)
                    .mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
    }
}