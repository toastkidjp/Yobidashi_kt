package jp.toastkid.yobidashi.libs.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Common HTTP client factory.
 *
 * @author toastkidjp
 */
object HttpClientFactory {
    fun make(): OkHttpClient {
        return withTimeout(5L)
    }

    fun withTimeout(seconds: Long): OkHttpClient =
            OkHttpClient.Builder()
                    .cookieJar(WebViewCookieHandler)
                    .connectTimeout(seconds, TimeUnit.SECONDS)
                    .readTimeout(seconds, TimeUnit.SECONDS)
                    .writeTimeout(seconds, TimeUnit.SECONDS)
                    .build()
}