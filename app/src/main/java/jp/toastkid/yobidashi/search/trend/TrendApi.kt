/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.trend

import androidx.annotation.WorkerThread
import retrofit2.Retrofit
import java.io.IOException
import kotlin.jvm.Throws

/**
 * @author toastkidjp
 */
class TrendApi {

    private val converter = TrendResponseConverterFactory.create()

    @Throws(IOException::class)
    @WorkerThread
    operator fun invoke(): List<Trend>? {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://trends.google.co.jp/")
                .addConverterFactory(converter)
                .build()

        val service = retrofit.create(TrendService::class.java)
        val call = service.call()
        return call.execute().body()
    }

}