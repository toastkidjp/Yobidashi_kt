package jp.toastkid.yobidashi.search.suggestion

import android.net.Uri
import jp.toastkid.yobidashi.libs.MultiByteCharacterInspector
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.util.*

/**
 * Suggest Web API response fetcher.
 *
 * @author toastkidjp
 */
class SuggestionFetcher {

    /**
     * HTTP client.
     */
    private val httpClient = HttpClientFactory.withTimeout(3L)

    /**
     * Response parser.
     */
    private val suggestionParser = SuggestionParser()

    private val multiByteCharacterInspector = MultiByteCharacterInspector()

    /**
     * Fetch Web API result asynchronously.
     *
     * @param query
     * @param listCallback
     */
    fun fetchAsync(query: String, listCallback: (List<String>) -> Unit) {
        val request = Request.Builder()
                .url(makeSuggestUrl(query))
                .build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = Timber.e(e)

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string() ?: return
                listCallback(suggestionParser(body))
            }
        })
    }

    /**
     * Make suggest Web API requesting URL.
     *
     * @param query Query
     * @return suggest Web API requesting URL
     */
    private fun makeSuggestUrl(query: String): String {
        return "$URL&hl=${findHl(query)}&q=${Uri.encode(query)}"
    }

    /**
     * Find appropriate language.
     *
     * @param query
     * @return language (ex: "ja", "en")
     */
    private fun findHl(query: String): String {
        return if (multiByteCharacterInspector(query)) "ja" else Locale.getDefault().language
    }

    companion object {

        /**
         * Suggest Web API.
         */
        private const val URL = "https://www.google.com/complete/search?&output=toolbar"
    }
}
