package jp.toastkid.jitte.libs.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Common HTTP client factory.
 *
 * @author toastkidjp
 */
object HttpClientFactory {
    fun make(): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build()
    }
}