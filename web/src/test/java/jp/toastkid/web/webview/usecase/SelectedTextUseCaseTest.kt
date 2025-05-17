/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.webview.usecase

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
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import org.junit.After
import org.junit.Before
import org.junit.Test

class SelectedTextUseCaseTest {

    @InjectMockKs
    private lateinit var selectedTextUseCase: SelectedTextUseCase

    @MockK
    private lateinit var stringResolver: (Int, Any) -> String

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { stringResolver.invoke(any(), any()) }.returns("Count: 4")
        every { contentViewModel.snackShort(any<String>()) }.just(Runs)
        every { contentViewModel.snackShort(any<Int>()) }.just(Runs)
        every { contentViewModel.preview(any()) }.just(Runs)
        every { contentViewModel.search(any()) }.just(Runs)

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
        verify(exactly = 0) { contentViewModel.open(any()) }
        verify(exactly = 0) { contentViewModel.preview(any()) }
    }

    @Test
    fun search() {
        selectedTextUseCase.search("test")

        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 1) { contentViewModel.search(any()) }
        verify(exactly = 0) { contentViewModel.preview(any()) }
    }

    @Test
    fun searchWithPreview() {
        selectedTextUseCase.searchWithPreview("test")

        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { contentViewModel.search(any()) }
        verify(exactly = 1) { contentViewModel.preview(any()) }
    }

    @Test
    fun searchUrl() {
        every { Urls.isValidUrl(any()) }.returns(true)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        selectedTextUseCase.search("https://www.yahoo.co.jp")

        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 1) { contentViewModel.search(any()) }
        verify(exactly = 0) { contentViewModel.preview(any()) }
    }

    @Test
    fun searchWithPreviewUrl() {
        every { Urls.isValidUrl(any()) }.returns(true)
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        selectedTextUseCase.searchWithPreview("https://www.yahoo.co.jp")

        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { contentViewModel.search(any()) }
        verify(exactly = 1) { contentViewModel.preview(any()) }
    }

}