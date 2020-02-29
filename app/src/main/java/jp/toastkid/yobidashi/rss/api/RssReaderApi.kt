/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.api

import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import jp.toastkid.yobidashi.rss.model.Rss
import retrofit2.Retrofit

/**
 * @author toastkidjp
 */
class RssReaderApi {

    private val converter = RssConverterFactory.create()

    @WorkerThread
    operator fun invoke(rssUrl: String): Rss? {
        val uri = rssUrl.toUri()
        val retrofit = Retrofit.Builder()
                .baseUrl("${uri.scheme}://${uri.host}")
                .addConverterFactory(converter)
                .build()

        val service = retrofit.create(RssService::class.java)
        val call = service.call(rssUrl)
        return call.execute().body()
    }
}