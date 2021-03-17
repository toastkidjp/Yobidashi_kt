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
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test

class TrendResponseConverterTest {

    @InjectMockKs
    private lateinit var trendResponseConverter: TrendResponseConverter

    @MockK
    private lateinit var parser: TrendParser

    @MockK
    private lateinit var responseBody: ResponseBody

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { parser.invoke(any()) }.returns(mockk())
        every { responseBody.string() }.returns("test")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testConvert() {
        trendResponseConverter.convert(responseBody)

        verify(exactly = 1) { parser.invoke(any()) }
        verify(exactly = 1) { responseBody.string() }
    }

}