/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.url_suggestion

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistory
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class ItemDeletionUseCaseTest {

    @InjectMockKs
    private lateinit var itemDeletionUseCase: ItemDeletionUseCase

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var viewHistoryRepository: ViewHistoryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { bookmarkRepository.delete(any()) }.returns(Unit)
        every { viewHistoryRepository.delete(any()) }.returns(Unit)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testBookmarkCase() {
        itemDeletionUseCase.invoke(Bookmark())

        verify(exactly = 1) { bookmarkRepository.delete(any()) }
        verify(exactly = 0) { viewHistoryRepository.delete(any()) }
    }

    @Test
    fun testViewHistoryCase() {
        itemDeletionUseCase.invoke(ViewHistory())

        verify(exactly = 0) { bookmarkRepository.delete(any()) }
        verify(exactly = 1) { viewHistoryRepository.delete(any()) }
    }

}