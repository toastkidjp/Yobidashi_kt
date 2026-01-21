/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api

import jp.toastkid.api.lib.HttpClientFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.io.InputStream

class DownloadApi {

    private val httpClient = HttpClientFactory().withTimeout(3)

    operator fun invoke(url: String, callback: (InputStream?) -> Unit) {
        httpClient.newCall(Request.Builder().url(url).get().build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Timber.e(e)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.body.byteStream())
            }

        })
    }

}