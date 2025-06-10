/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.api.wikipedia

import androidx.annotation.WorkerThread
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import jp.toastkid.api.wikipedia.model.Article
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

/**
 * Wikipedia API.
 *
 * @author toastkidjp
 */
class WikipediaApi(
    /**
     * Wikipedia URL decider.
     */
    private val baseUrl: String
) {

    private val mediaType = "application/json".toMediaType()

    private val json = Json { ignoreUnknownKeys = true }

    private val jsonConverterFactory = json.asConverterFactory(mediaType)

    /**
     * You should call this method on worker-thread.
     */
    @WorkerThread
    operator fun invoke(): Array<Article>? {
        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(
                    jsonConverterFactory
                )
                .build()

        val service = retrofit.create(WikipediaService::class.java)
        val call = service.call()
        return call.execute().body()?.query?.random
    }

}