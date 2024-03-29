/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.rss

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import jp.toastkid.api.rss.model.Parser
import jp.toastkid.api.rss.model.Rss
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class RssResponseConverterTest {

    @MockK
    private lateinit var parser: Parser

    @MockK
    private lateinit var responseBody: ResponseBody

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        every { parser.parse(any()) }.answers { Rss() }

        val converter = RssResponseConverter(parser)
        every { responseBody.close() }.just(Runs)
        every { responseBody.string() }.answers { "" }

        converter.convert(responseBody)

        verify(exactly = 1) { parser.parse(any()) }
        verify(exactly = 1) { responseBody.string() }
        verify(exactly = 1) { responseBody.close() }
    }
}