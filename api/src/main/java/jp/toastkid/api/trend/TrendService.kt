/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.api.trend

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author toastkidjp
 */
interface TrendService {

    @GET("trending/rss")
    fun call(@Query("geo") pn: String): Call<List<Trend>?>

}