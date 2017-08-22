package jp.toastkid.jitte.search.suggestion

import android.net.Uri

import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

import io.reactivex.functions.Consumer
import jp.toastkid.jitte.libs.Strings
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Suggest Web API response fetcher.

 * @author toastkidjp
 */
class SuggestionFetcher {

    /** HTTP client.  */
    private val mClient: OkHttpClient

    /**
     * Initialize HTTP client.
     */
    init {
        mClient = OkHttpClient.Builder()
                .connectTimeout(3L, TimeUnit.SECONDS)
                .readTimeout(3L, TimeUnit.SECONDS)
                .build()
    }

    /**
     * Fetch Web API result asynchronously.

     * @param query
     * *
     * @param consumer
     */
    fun fetchAsync(query: String, consumer: Consumer<List<String>>) {
        val request = Request.Builder()
                .url(makeSuggestUrl(query))
                .build()
        mClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.body() == null) {
                    return
                }
                try {
                    consumer.accept(SuggestionParser().parse(response.body()!!.string()))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
    }

    /**
     * Make suggest Web API requesting URL.
     * @param query Query
     * *
     * @return suggest Web API requesting URL
     */
    private fun makeSuggestUrl(query: String): String {
        return URL + "&hl=" + findHl(query) + "&q=" + Uri.encode(query)
    }

    /**
     * Find appropriate language.
     * @param query
     * *
     * @return
     */
    private fun findHl(query: String): String {
        return if (Strings.containsMultiByte(query)) "ja" else Locale.getDefault().language
    }

    companion object {

        /** Suggest Web API.  */
        private val URL = "https://www.google.com/complete/search?&output=toolbar"
    }
}
