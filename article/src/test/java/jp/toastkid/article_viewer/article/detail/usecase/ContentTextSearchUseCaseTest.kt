/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.detail.usecase

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import jp.toastkid.lib.view.TextViewHighlighter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import org.junit.After
import org.junit.Before
import org.junit.Test

class ContentTextSearchUseCaseTest {

    @InjectMockKs
    private lateinit var contentTextSearchUseCase: ContentTextSearchUseCase

    @MockK
    private lateinit var textViewHighlighter: TextViewHighlighter

    @MockK
    private lateinit var inputChannel: Channel<String>

    @Suppress("unused")
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { inputChannel.send(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        contentTextSearchUseCase.invoke("test")

        coVerify(exactly = 1) { inputChannel.send(any()) }
    }

    @Test
    fun testBlankCase() {
        contentTextSearchUseCase.invoke(" ")

        coVerify(exactly = 0) { inputChannel.send(any()) }
    }

    @Test
    fun testStartObserve() {
        contentTextSearchUseCase.startObserve()
    }

}