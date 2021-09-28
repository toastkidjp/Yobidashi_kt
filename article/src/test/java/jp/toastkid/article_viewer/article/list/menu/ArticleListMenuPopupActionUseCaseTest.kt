/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.menu

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

class ArticleListMenuPopupActionUseCaseTest {

    @InjectMockKs
    private lateinit var articleListMenuPopupActionUseCase: ArticleListMenuPopupActionUseCase

    @MockK
    private lateinit var articleRepository: ArticleRepository

    @MockK
    private lateinit var bookmarkRepository: BookmarkRepository

    @MockK
    private lateinit var deleted: (Article) -> Unit

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { bookmarkRepository.add(any()) }.just(Runs)
        coEvery { articleRepository.findArticleById(any()) }.returns(mockk())
        coEvery { articleRepository.delete(any()) }.just(Runs)
        coEvery { deleted(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun addToBookmark() {
        articleListMenuPopupActionUseCase.addToBookmark(1)

        coVerify(exactly = 1) { bookmarkRepository.add(any()) }
    }

}