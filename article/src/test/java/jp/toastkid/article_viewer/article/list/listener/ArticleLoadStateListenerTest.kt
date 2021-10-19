/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.listener

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test

class ArticleLoadStateListenerTest {

    @InjectMockKs
    private lateinit var articleLoadStateListener: ArticleLoadStateListener

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var countSupplier: () -> Int

    @MockK
    private lateinit var stringResolver: (Int) -> String

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { countSupplier.invoke() }.returns(10)
        every { contentViewModel.snackShort(any<String>()) }.just(Runs)
        every { stringResolver.invoke(any()) }.returns("test %d count.")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        val combinedLoadStates = CombinedLoadStates(
            LoadStates(
                LoadState.Loading,
                LoadState.NotLoading(false),
                LoadState.NotLoading(false)
            ),
            null
        )

        articleLoadStateListener.invoke(combinedLoadStates)

        verify(exactly = 0) { countSupplier.invoke() }
        verify(exactly = 0) { contentViewModel.snackShort(any<String>()) }
        verify(exactly = 0) { stringResolver.invoke(any()) }

        val completeLoadStates = makeCompleteStatus()

        articleLoadStateListener.invoke(completeLoadStates)

        verify(exactly = 1) { countSupplier.invoke() }
        verify(exactly = 1) { contentViewModel.snackShort(any<String>()) }
        verify(exactly = 1) { stringResolver.invoke(any()) }
    }

    @Test
    fun testCannotInvokedCase() {
        val combinedLoadStates = CombinedLoadStates(
            LoadStates(
                LoadState.NotLoading(false),
                LoadState.Loading,
                LoadState.NotLoading(false)
            ),
            null
        )

        articleLoadStateListener.invoke(combinedLoadStates)

        verify(exactly = 0) { countSupplier.invoke() }
        verify(exactly = 0) { contentViewModel.snackShort(any<String>()) }
        verify(exactly = 0) { stringResolver.invoke(any()) }

        val completeLoadStates = makeCompleteStatus()

        articleLoadStateListener.invoke(completeLoadStates)

        verify(exactly = 0) { countSupplier.invoke() }
        verify(exactly = 0) { contentViewModel.snackShort(any<String>()) }
        verify(exactly = 0) { stringResolver.invoke(any()) }
    }

    private fun makeCompleteStatus(): CombinedLoadStates {
        val completeLoadStates = CombinedLoadStates(
            LoadStates(
                LoadState.NotLoading(false),
                LoadState.NotLoading(false),
                LoadState.NotLoading(false)
            ),
            null
        )
        return completeLoadStates
    }

}