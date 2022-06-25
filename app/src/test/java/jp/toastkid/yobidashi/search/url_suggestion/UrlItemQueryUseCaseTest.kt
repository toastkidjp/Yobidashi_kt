/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.url_suggestion

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistory
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class UrlItemQueryUseCaseTest {

    private lateinit var urlItemQueryUseCase: UrlItemQueryUseCase

    @MockK
    private lateinit var submitItems: (List<UrlItem>) -> Unit

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var viewHistoryRepository: ViewHistoryRepository

    @MockK
    private lateinit var switchVisibility: (Boolean) -> Unit

    @MockK
    private lateinit var rtsSuggestionUseCase: RtsSuggestionUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { submitItems.invoke(any()) }.returns(Unit)
        coEvery { bookmarkRepository.search(any(), any()) }.returns(listOf(Bookmark()))
        coEvery { viewHistoryRepository.search(any(), any()) }.returns(listOf(ViewHistory()))
        coEvery { switchVisibility.invoke(any()) }.returns(Unit)
        coEvery { rtsSuggestionUseCase.invoke(any(), any()) }.just(Runs)

        urlItemQueryUseCase = UrlItemQueryUseCase(
            submitItems, bookmarkRepository, viewHistoryRepository, switchVisibility,
            rtsSuggestionUseCase,
            Dispatchers.Unconfined, Dispatchers.Unconfined
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        urlItemQueryUseCase.invoke("test")

        coVerify(exactly = 1) { submitItems.invoke(any()) }
        coVerify(exactly = 1) { bookmarkRepository.search(any(), any()) }
        coVerify(exactly = 1) { viewHistoryRepository.search(any(), any()) }
        coVerify { rtsSuggestionUseCase.invoke(any(), any()) }
    }

    @Test
    fun testBlankCase() {
        urlItemQueryUseCase.invoke(" ")

        coVerify(exactly = 1) { submitItems.invoke(any()) }
        coVerify(exactly = 0) { bookmarkRepository.search(any(), any()) }
        coVerify(exactly = 1) { viewHistoryRepository.search(any(), any()) }
        coVerify { rtsSuggestionUseCase.invoke(any(), any()) }
    }

}