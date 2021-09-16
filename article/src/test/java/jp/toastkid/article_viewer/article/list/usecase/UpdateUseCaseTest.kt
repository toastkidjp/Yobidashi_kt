/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.usecase

import android.content.Context
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.article_viewer.article.list.ArticleListFragmentViewModel
import jp.toastkid.article_viewer.zip.ZipLoaderService
import org.junit.After
import org.junit.Before
import org.junit.Test

class UpdateUseCaseTest {

    @InjectMockKs
    private lateinit var updateUseCase: UpdateUseCase

    @MockK
    private lateinit var viewModel: ArticleListFragmentViewModel

    @MockK
    private lateinit var contextProvider: () -> Context?

    @MockK
    private lateinit var uri: Uri

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { viewModel.showProgress() }.just(Runs)
        every { contextProvider.invoke() }.returns(mockk())

        mockkObject(ZipLoaderService)
        every { ZipLoaderService.start(any(), any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invokeIfNeed() {
        updateUseCase.invokeIfNeed(uri)

        verify(exactly = 1) { viewModel.showProgress() }
        verify(exactly = 1) { contextProvider.invoke() }
        verify(exactly = 1) { ZipLoaderService.start(any(), any()) }
    }

}