/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.usecase

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
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.search.UrlFactory
import org.junit.After
import org.junit.Before
import org.junit.Test

class SelectedTextUseCaseTest {

    @InjectMockKs
    private lateinit var selectedTextUseCase: SelectedTextUseCase

    @MockK
    private lateinit var stringResolver: (Int, Any) -> String

    @MockK
    private lateinit var urlFactory: UrlFactory

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var browserViewModel: BrowserViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { stringResolver.invoke(any(), any()) }.returns("Count: 4")
        every { contentViewModel.snackShort(any<String>()) }.just(Runs)
        every { contentViewModel.snackShort(any<Int>()) }.answers { Unit }
        every { browserViewModel.open(any()) }.answers { Unit }
        every { browserViewModel.preview(any()) }.answers { Unit }
        every { urlFactory.invoke(any(), any()) }.returns(mockk())

        mockkObject(Urls)
        every { Urls.isValidUrl(any()) }.returns(false)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun countCharacters() {
        selectedTextUseCase.countCharacters("\"test\"")

        verify(exactly = 1) { contentViewModel.snackShort(any<String>()) }
        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { browserViewModel.open(any()) }
        verify(exactly = 0) { browserViewModel.preview(any()) }
        verify(exactly = 0) { urlFactory.invoke(any(), any()) }
    }

    @Test
    fun search() {
        selectedTextUseCase.search("test", "test")

        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 1) { browserViewModel.open(any()) }
        verify(exactly = 0) { browserViewModel.preview(any()) }
        verify(exactly = 1) { urlFactory.invoke(any(), any()) }
    }

    @Test
    fun searchWithPreview() {
        selectedTextUseCase.searchWithPreview("test", "test")

        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { browserViewModel.open(any()) }
        verify(exactly = 1) { browserViewModel.preview(any()) }
        verify(exactly = 1) { urlFactory.invoke(any(), any()) }
    }

    @Test
    fun searchUrl() {
        every { Urls.isValidUrl(any()) }.returns(true)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        selectedTextUseCase.search("https://www.yahoo.co.jp", "test")

        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 1) { browserViewModel.open(any()) }
        verify(exactly = 0) { browserViewModel.preview(any()) }
        verify(exactly = 0) { urlFactory.invoke(any(), any()) }
        verify(exactly = 1) { Uri.parse(any()) }
    }

    @Test
    fun searchWithEmptyWord() {
        selectedTextUseCase.search("", "test")

        verify(exactly = 1) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { browserViewModel.open(any()) }
        verify(exactly = 0) { browserViewModel.preview(any()) }
        verify(exactly = 0) { urlFactory.invoke(any(), any()) }
    }

    @Test
    fun searchWithOnlyDoubleQuotes() {
        selectedTextUseCase.search("\"\"", "test")

        verify(exactly = 1) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { browserViewModel.open(any()) }
        verify(exactly = 0) { browserViewModel.preview(any()) }
        verify(exactly = 0) { urlFactory.invoke(any(), any()) }
    }

}