/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.trend

import android.net.Uri
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class TrendParserTest {

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.encode(any()) } returns "encoded"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        val resourceAsStream = javaClass.classLoader?.getResourceAsStream("hot_trend/hourly.xml")

        val xmlSource = resourceAsStream?.source()?.buffer()?.use { it.readUtf8() }
                ?: throw RuntimeException()

        val items = TrendParser().invoke(xmlSource)
        assertEquals(20, items.size)
        assertTrue(items.first().link.isNotBlank())
    }

}