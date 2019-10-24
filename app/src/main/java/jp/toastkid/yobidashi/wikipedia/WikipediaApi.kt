/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.wikipedia

import androidx.annotation.WorkerThread
import jp.toastkid.yobidashi.wikipedia.model.Article
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * @author toastkidjp
 */
class WikipediaApi {

    private val urlDecider = UrlDecider()

    private val converterFactory = MoshiConverterFactory.create()

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