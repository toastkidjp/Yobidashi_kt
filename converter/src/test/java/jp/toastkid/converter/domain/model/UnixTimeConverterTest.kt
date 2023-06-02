/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.converter.domain.model

import android.text.format.DateFormat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnixTimeConverterTest {

    @InjectMockKs
    private lateinit var unixTimeConverter: UnixTimeConverter

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(DateFormat::class)
        every { DateFormat.format(any(), any<Long>()) } returns "2023-04-26 09:54:56"
    }

    @Test
    fun title() {
        assertTrue(unixTimeConverter.title().isNotBlank())
    }

    @Test
    fun test() {
        assertEquals("1682470496000", unixTimeConverter.secondInputAction("2023-04-26 09:54:56"))
        assertEquals("2023-04-26 09:54:56", unixTimeConverter.firstInputAction("1682470496000"))
    }

}