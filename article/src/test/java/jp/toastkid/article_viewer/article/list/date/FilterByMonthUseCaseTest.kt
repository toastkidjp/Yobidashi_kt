/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.date

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test

class FilterByMonthUseCaseTest {

    @InjectMockKs
    private lateinit var filterByMonthUseCase: FilterByMonthUseCase

    @MockK
    private lateinit var articleListFragmentViewModel: ArticleListFragmentViewModel

    @MockK
    private lateinit var monthFormatterUseCase: MonthFormatterUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { articleListFragmentViewModel.filter(any()) }.answers { Unit }
        every { monthFormatterUseCase.invoke(any()) }.returns("02")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        filterByMonthUseCase.invoke(2021, 1)

        verify(exactly = 1) { articleListFragmentViewModel.filter(any()) }
        verify(exactly = 1) { monthFormatterUseCase.invoke(any()) }
    }

}