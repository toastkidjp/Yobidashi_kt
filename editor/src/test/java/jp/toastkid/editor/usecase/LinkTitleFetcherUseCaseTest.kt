/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.usecase

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URL

class LinkTitleFetcherUseCaseTest {

    @InjectMockKs
    private lateinit var linkTitleFetcherUseCase: LinkTitleFetcherUseCase

    @MockK
    private lateinit var document: Document

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { document.title() }.returns("title")

        mockkStatic(Jsoup::class)
        every { Jsoup.parse(any<URL>(), any()) }.returns(document)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testWithMock() {
        val title = linkTitleFetcherUseCase.invoke("https://www.yahoo.co.jp")

        assertEquals("[title](https://www.yahoo.co.jp)", title)
        verify(exactly = 1) { Jsoup.parse(any<URL>(), any()) }
        verify(exactly = 1) { document.title() }
    }

    @Test
    fun testOccurredSocketTimeoutExceptionCase() {
        every { Jsoup.parse(any<URL>(), any()) }.throws(SocketTimeoutException())

        val title = linkTitleFetcherUseCase.invoke("https://www.yahoo.co.jp")

        assertEquals("https://www.yahoo.co.jp", title)
        verify(exactly = 1) { Jsoup.parse(any<URL>(), any()) }
    }

    @Test
    fun testOccurredIOExceptionCase() {
        every { Jsoup.parse(any<URL>(), any()) }.throws(IOException())

        val title = linkTitleFetcherUseCase.invoke("https://www.yahoo.co.jp")

        assertEquals("https://www.yahoo.co.jp", title)
        verify { Jsoup.parse(any<URL>(), any()) }
    }

    @Ignore("Because this function is used network connection.")
    fun testWithRealNetwork() {
        assertEquals(
            "投資信託のモーニングスター｜投資信託・株式・国内/海外ＥＴＦ（上場投資信託）・為替/指数　マーケット情報サイト",
            LinkTitleFetcherUseCase()
                .invoke("https://www.morningstar.co.jp")
        )
    }

}