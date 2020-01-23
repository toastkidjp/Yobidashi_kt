/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * @author toastkidjp
 */
interface RssService {

    @GET
    fun call(@Url url: String): Call<Rss?>
}