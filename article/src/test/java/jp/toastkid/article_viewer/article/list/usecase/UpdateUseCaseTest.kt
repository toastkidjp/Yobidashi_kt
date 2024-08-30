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
import jp.toastkid.article_viewer.article.list.ArticleListViewModel
import jp.toastkid.article_viewer.zip.ZipLoaderWorker
import org.junit.After
import org.junit.Before
import org.junit.Test

class UpdateUseCaseTest {

    @InjectMockKs
    private lateinit var updateUseCase: UpdateUseCase

    @MockK
    private lateinit var viewModel: ArticleListViewModel

    @MockK
    private lateinit var contextProvider: () -> Context?

    @MockK
    private lateinit var uri: Uri

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { viewModel.showProgress() }.just(Runs)
        every { contextProvider.invoke() }.returns(mockk())

        mockkObject(ZipLoaderWorker)
        every { ZipLoaderWorker.start(any(), any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvokeIfNeed() {
        updateUseCase.invokeIfNeed(uri)

        verify(exactly = 1) { viewModel.showProgress() }
        verify(exactly = 1) { contextProvider.invoke() }
        verify(exactly = 1) { ZipLoaderWorker.start(any(), any()) }
    }

    @Test
    fun testInvokeIfNotNeed() {
        updateUseCase.invokeIfNeed(null)

        verify(exactly = 0) { viewModel.showProgress() }
        verify(exactly = 0) { contextProvider.invoke() }
        verify(exactly = 0) { ZipLoaderWorker.start(any(), any()) }
    }

    @Test
    fun testInvokeIfNotNeedContextIsNull() {
        every { contextProvider.invoke() }.returns(null)

        updateUseCase.invokeIfNeed(uri)

        verify(exactly = 1) { viewModel.showProgress() }
        verify(exactly = 1) { contextProvider.invoke() }
        verify(exactly = 0) { ZipLoaderWorker.start(any(), any()) }
    }

}