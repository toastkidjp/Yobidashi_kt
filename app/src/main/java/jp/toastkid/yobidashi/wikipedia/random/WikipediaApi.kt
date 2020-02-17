/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.wikipedia.random

import androidx.annotation.WorkerThread
import jp.toastkid.yobidashi.wikipedia.random.model.Article
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Wikipedia API.
 *
 * @author toastkidjp
 */
class WikipediaApi {

    /**
     * Wikipedia URL decider.
     */
    private val urlDecider = UrlDecider()

    /**
     * Converter factory.
     */
    private val converterFactory = MoshiConverterFactory.create()

    /**
     * You should call this method on worker-thread.
     */
    @WorkerThread
    fun invoke(): Array<Article>? {
        val retrofit = Retrofit.Builder()
                .baseUrl(urlDecider())
                .addConverterFactory(converterFactory)
                .build()

        val service = retrofit.create(WikipediaService::class.java)
        val call = service.call()
        return call.execute().body()?.query?.random
    }
}