/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.lib

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Common HTTP client factory.
 *
 * @author toastkidjp
 */
object HttpClientFactory {

    fun withTimeout(seconds: Long): OkHttpClient =
            OkHttpClient.Builder()
                    .cookieJar(WebViewCookieHandler())
                    .connectTimeout(seconds, TimeUnit.SECONDS)
                    .readTimeout(seconds, TimeUnit.SECONDS)
                    .writeTimeout(seconds, TimeUnit.SECONDS)
                    .build()
}