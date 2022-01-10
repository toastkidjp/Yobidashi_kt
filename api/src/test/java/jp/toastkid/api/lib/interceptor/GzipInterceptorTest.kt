/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.lib.interceptor

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Before
import org.junit.Test

class GzipInterceptorTest {

    @InjectMockKs
    private lateinit var gzipInterceptor: GzipInterceptor

    @MockK
    private lateinit var chain: Interceptor.Chain

    private lateinit var originalRequest: Request

    @MockK
    private lateinit var builder: Request.Builder

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        originalRequest = spyk(Request.Builder().url("https://www.yahoo.co.jp").build())

        every { chain.request() }.returns(originalRequest)
        every { chain.proceed(any()) }.returns(mockk())
        every { originalRequest getProperty "body" }.returns("tomato".toRequestBody())
        every { originalRequest getProperty "method" }.returns("get")
        every { originalRequest.newBuilder() }.returns(builder)
        every { builder.header(any(), any()) }.returns(builder)
        every { builder.method(any(), any()) }.returns(builder)
        every { builder.build() }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testCannotIntercept() {
        every { originalRequest getProperty "body" }.returns(null)

        gzipInterceptor.intercept(chain)

        verify { chain.proceed(any()) }
        verify(inverse = true) { originalRequest.newBuilder() }
    }

}