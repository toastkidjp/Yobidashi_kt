/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.wikipedia.random

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import jp.toastkid.yobidashi.wikipedia.random.model.Article
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale

class RandomWikipediaTest {

    @InjectMockKs
    private lateinit var randomWikipedia: RandomWikipedia

    @MockK
    private lateinit var wikipediaApi: WikipediaApi

    @MockK
    private lateinit var urlDecider: UrlDecider

    @Suppress("unused")
    private val mainDispatcher = Dispatchers.Unconfined

    @Suppress("unused")
    private val ioDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { wikipediaApi.invoke() }
                .answers { arrayOf(Article(1L, 0, "test")) }

        coEvery { urlDecider.invoke() }
                .answers { "https://${Locale.getDefault().language}.wikipedia.org/" }

        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.answers { mockk() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testFetchWithAction() {
        randomWikipedia.fetchWithAction { s, uri ->  }

        coVerify (atLeast = 1) { wikipediaApi.invoke() }
        coVerify (atLeast = 1) { urlDecider.invoke() }
        coVerify (atLeast = 1) { Uri.parse(any()) }
    }

}