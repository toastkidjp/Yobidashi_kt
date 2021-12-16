/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.suggestion

import android.net.Uri
import jp.toastkid.api.lib.HttpClientFactory
import jp.toastkid.api.lib.MultiByteCharacterInspector
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.util.Locale

/**
 * Suggest Web API response fetcher.
 *
 * @author toastkidjp
 */
class SuggestionApi(
    /**
     * HTTP client.
     */
    private val httpClient: OkHttpClient = HttpClientFactory().withTimeout(3L),
    /**
     * Response parser.
     */
    private val suggestionParser: SuggestionParser = SuggestionParser(),
    private val multiByteCharacterInspector: MultiByteCharacterInspector = MultiByteCharacterInspector()
) {

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
                val body = response.body?.string() ?: return
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
    private fun findHl(query: String): String =
            if (multiByteCharacterInspector(query)) "ja" else Locale.getDefault().language

    companion object {

        /**
         * Suggest Web API.
         */
        private const val URL = "https://www.google.com/complete/search?&output=toolbar"
    }
}
