/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api

import jp.toastkid.api.lib.HttpClientFactory
import okhttp3.Callback
import okhttp3.Request

class DownloadApi {

    private val httpClient = HttpClientFactory().withTimeout(3)

    operator fun invoke(url: String, callback: Callback) {
        httpClient.newCall(Request.Builder().url(url).get().build()).enqueue(callback)
    }

}