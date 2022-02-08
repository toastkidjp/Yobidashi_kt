/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.usecase

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.UrlFactory
import org.junit.After
import org.junit.Before
import org.junit.Test

class WebSearchResultTabOpenerUseCaseTest {

    @InjectMockKs
    private lateinit var webSearchResultTabOpenerUseCase: WebSearchResultTabOpenerUseCase

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var openNewWebTab: (Uri) -> Unit

    @MockK
    private lateinit var urlFactory: UrlFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { openNewWebTab.invoke(any()) }.just(Runs)
        every { preferenceApplier.getDefaultSearchEngine() }.returns("default")
        every { urlFactory.invoke(any(), any()) }.returns(mockk())

        mockkObject(Urls)
        every { Urls.isValidUrl(any()) }.returns(true)

        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testLaunchSearchCase() {
        every { Urls.isValidUrl(any()) }.returns(false)

        webSearchResultTabOpenerUseCase.invoke("test")

        verify(inverse = true) { Uri.parse(any()) }
        verify { urlFactory.invoke(any(), any()) }
        verify { openNewWebTab.invoke(any()) }
    }

    @Test
    fun testWebUrlCase() {
        webSearchResultTabOpenerUseCase.invoke("test")

        verify { Uri.parse(any()) }
        verify(inverse = true) { urlFactory.invoke(any(), any()) }
        verify { openNewWebTab.invoke(any()) }
    }

    @Test
    fun test() {
        every { Urls.isValidUrl(any()) }.returns(false)
        every { preferenceApplier.getDefaultSearchEngine() }.returns(null)

        webSearchResultTabOpenerUseCase.invoke("test")

        verify(inverse = true) { Uri.parse(any()) }
        verify(inverse = true) { urlFactory.invoke(any(), any()) }
        verify(inverse = true) { openNewWebTab.invoke(any()) }
    }
}