/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.rss.api

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.rss.model.Rss
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit

class RssReaderApiTest {

    private lateinit var rssReaderApi: RssReaderApi

    @RelaxedMockK
    private lateinit var uri: Uri

    @MockK
    private lateinit var retrofit: Retrofit

    @MockK
    private lateinit var builder: Retrofit.Builder

    @MockK
    private lateinit var service: RssService

    @MockK
    private lateinit var call: Call<Rss?>

    @MockK
    private lateinit var response: Response<Rss?>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(uri)

        mockkConstructor(Retrofit.Builder::class)
        every { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) }.returns(builder)
        every { builder.addConverterFactory(any()) }.returns(builder)
        every { builder.build() }.returns(retrofit)

        every { retrofit.create(any<Class<RssService>>()) }.returns(service)
        every { service.call(any()) }.returns(call)
        every { call.execute() }.returns(response)
        every { response.body() }.returns(mockk())

        rssReaderApi = RssReaderApi()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        rssReaderApi.invoke("https://www.yahoo-test.com")

        verify(exactly = 1) { Uri.parse(any()) }
        verify(exactly = 1) { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) }
        verify(exactly = 1) { builder.addConverterFactory(any()) }
        verify(exactly = 1) { builder.build() }
        verify(exactly = 1) { retrofit.create(any<Class<RssService>>()) }
        verify(exactly = 1) { service.call(any()) }
        verify(exactly = 1) { call.execute() }
        verify(exactly = 1) { response.body() }
    }

}