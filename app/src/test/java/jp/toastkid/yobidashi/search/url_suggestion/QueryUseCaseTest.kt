/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.url_suggestion

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
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
class QueryUseCaseTest {

    private lateinit var queryUseCase: QueryUseCase

    @MockK
    private lateinit var adapter: Adapter

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var viewHistoryRepository: ViewHistoryRepository

    @MockK
    private lateinit var switchVisibility: (Boolean) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { adapter.clear() }.returns(Unit)
        coEvery { adapter.add(any()) }.returns(Unit)
        coEvery { adapter.isNotEmpty() }.returns(true)
        coEvery { adapter.notifyDataSetChanged() }.returns(Unit)
        coEvery { bookmarkRepository.search(any(), any()) }.returns(listOf(Bookmark()))
        coEvery { viewHistoryRepository.search(any(), any()) }.returns(listOf(ViewHistory()))
        coEvery { switchVisibility.invoke(any()) }.returns(Unit)

        queryUseCase = QueryUseCase(
            adapter, bookmarkRepository, viewHistoryRepository, switchVisibility,
            Dispatchers.Unconfined, Dispatchers.Unconfined
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        queryUseCase.invoke("test")

        coVerify(exactly = 1) { adapter.clear() }
        coVerify(atLeast = 1) { adapter.add(any()) }
        coVerify(exactly = 1) { adapter.isNotEmpty() }
        coVerify(exactly = 1) { adapter.notifyDataSetChanged() }
        coVerify(exactly = 1) { bookmarkRepository.search(any(), any()) }
        coVerify(exactly = 1) { viewHistoryRepository.search(any(), any()) }
    }

    @Test
    fun testBlankCase() {
        queryUseCase.invoke(" ")

        coVerify(exactly = 1) { adapter.clear() }
        coVerify(atLeast = 1) { adapter.add(any()) }
        coVerify(exactly = 1) { adapter.isNotEmpty() }
        coVerify(exactly = 1) { adapter.notifyDataSetChanged() }
        coVerify(exactly = 0) { bookmarkRepository.search(any(), any()) }
        coVerify(exactly = 1) { viewHistoryRepository.search(any(), any()) }
    }

}