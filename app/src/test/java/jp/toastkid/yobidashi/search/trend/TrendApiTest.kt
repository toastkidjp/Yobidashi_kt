/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.trend

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit

class TrendApiTest {

    @InjectMockKs
    private lateinit var trendApi: TrendApi

    @MockK
    private lateinit var requestBuilder: Retrofit.Builder

    @MockK
    private lateinit var retrofit: Retrofit

    @MockK
    private lateinit var service: TrendService

    @MockK
    private lateinit var call: Call<List<Trend>?>

    @MockK
    private lateinit var response: retrofit2.Response<List<Trend>?>

    @MockK
    private lateinit var body: List<Trend>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(Retrofit.Builder::class)
        every { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) }.returns(requestBuilder)
        every { requestBuilder.addConverterFactory(any()) }.returns(requestBuilder)
        every { requestBuilder.build() }.returns(retrofit)
        every { retrofit.create(any<Class<TrendService>>()) }.returns(service)
        every { service.call() }.returns(call)
        every { call.execute() }.returns(response)
        every { response.body() }.returns(body)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        trendApi.invoke()

        verify(exactly = 1) { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) }
        verify(exactly = 1) { requestBuilder.addConverterFactory(any()) }
        verify(exactly = 1) { requestBuilder.build() }
        verify(exactly = 1) { retrofit.create(any<Class<TrendService>>()) }
        verify(exactly = 1) { service.call() }
        verify(exactly = 1) { call.execute() }
        verify(exactly = 1) { response.body() }
    }

}