/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.rss.extractor

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.Urls
import jp.toastkid.yobidashi.libs.network.HttpClientFactory
import okhttp3.Call
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test

class HtmlApiTest {

    private lateinit var htmlApi: HtmlApi

    @MockK
    private lateinit var httpClient: OkHttpClient

    @MockK
    private lateinit var call: Call

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(HttpClientFactory)
        every { HttpClientFactory.withTimeout(any()) }.returns(httpClient)
        every { httpClient.newCall(any()) }.returns(call)
        every { call.execute() }.returns(mockk())

        htmlApi = HtmlApi()

        mockkObject(Urls)
        every { Urls.isInvalidUrl(any()) }.returns(false)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        htmlApi.invoke("https://www.yahoo.co.jp")

        verify(exactly = 1) { HttpClientFactory.withTimeout(any()) }
        verify(exactly = 1) { httpClient.newCall(any()) }
        verify(exactly = 1) { call.execute() }
    }

}