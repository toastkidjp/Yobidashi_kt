/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.suggestion

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.libs.MultiByteCharacterInspector
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Before
import org.junit.Test

class SuggestionApiTest {

    @InjectMockKs
    private lateinit var suggestionApi: SuggestionApi

    @MockK
    private lateinit var httpClient: OkHttpClient

    @MockK
    private lateinit var suggestionParser: SuggestionParser

    @MockK
    private lateinit var multiByteCharacterInspector: MultiByteCharacterInspector

    @MockK
    private lateinit var builder: Request.Builder

    @MockK
    private lateinit var call: Call

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(Request.Builder::class)
        every { anyConstructed<Request.Builder>().url(any<String>()) }.returns(builder)
        every { builder.build() }.returns(mockk())

        every { httpClient.newCall(any()) }.returns(call)
        every { call.enqueue(any()) }.answers { Unit }

        every { multiByteCharacterInspector.invoke(any()) }.returns(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testFetchAsync() {
        suggestionApi.fetchAsync("query", {})

        verify(exactly = 1) { anyConstructed<Request.Builder>().url(any<String>()) }
        verify(exactly = 1) { builder.build() }
        verify(exactly = 1) { httpClient.newCall(any()) }
        verify(exactly = 1) { call.enqueue(any()) }
        verify(exactly = 1) { multiByteCharacterInspector.invoke(any()) }
    }

}