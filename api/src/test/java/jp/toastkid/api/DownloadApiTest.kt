/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.api.lib.HttpClientFactory
import okhttp3.OkHttpClient

class DownloadApiTest {

    private lateinit var downloadApi: DownloadApi

    @MockK
    private lateinit var httpClient: OkHttpClient

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(HttpClientFactory::class)
        every { anyConstructed<HttpClientFactory>().withTimeout(any()) }.returns(httpClient)
        every { httpClient.newCall(any()).enqueue(any()) }.just(Runs)

        downloadApi = DownloadApi()
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun invoke() {
        downloadApi.invoke("https://www.yahoo.co.jp", mockk())

        verify { httpClient.newCall(any()).enqueue(any()) }
    }
}