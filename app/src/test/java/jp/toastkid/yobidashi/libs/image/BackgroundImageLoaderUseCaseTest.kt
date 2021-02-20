/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.libs.image

import android.content.Context
import android.widget.ImageView
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class BackgroundImageLoaderUseCaseTest {

    @InjectMockKs
    private lateinit var backgroundImageLoaderUseCase: BackgroundImageLoaderUseCase

    @MockK
    private lateinit var fileResolver: (String) -> File

    @MockK
    private lateinit var targetView: ImageView

    @MockK
    private lateinit var file: File

    @MockK
    private lateinit var imageRequestBuilder: ImageRequest.Builder

    @MockK
    private lateinit var imageLoader: ImageLoader

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { fileResolver.invoke(any()) }.returns(file)

        mockkConstructor(ImageRequest.Builder::class)
        every { anyConstructed<ImageRequest.Builder>().data(any()) }.returns(imageRequestBuilder)
        every { imageRequestBuilder.target(any<ImageView>()) }.returns(imageRequestBuilder)
        every { imageRequestBuilder.size(any(), any()) }.returns(imageRequestBuilder)
        every { imageRequestBuilder.build() }.returns(mockk())
        mockkObject(Coil)
        every { Coil.imageLoader(any()) }.returns(imageLoader)
        every { imageLoader.enqueue(any()) }.returns(mockk())
        every { targetView.getContext() }.answers { context }
        every { targetView.getMeasuredWidth() }.returns(1)
        every { targetView.getMeasuredHeight() }.returns(1)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        backgroundImageLoaderUseCase.invoke(targetView, "test.png")

        verify(exactly = 1) { anyConstructed<ImageRequest.Builder>().data(any()) }
        verify(exactly = 1) { imageRequestBuilder.target(any<ImageView>()) }
        verify(exactly = 1) { imageRequestBuilder.size(any(), any()) }
        verify(exactly = 1) { imageRequestBuilder.build() }
        verify(exactly = 1) { imageLoader.enqueue(any()) }
        verify(atLeast = 1) { targetView.getContext() }
        verify(atLeast = 1) { targetView.getMeasuredWidth() }
        verify(atLeast = 1) { targetView.getMeasuredHeight() }
    }

    @Test
    fun testPathIsEmpty() {
        backgroundImageLoaderUseCase.invoke(targetView, "")

        verify(exactly = 0) { anyConstructed<ImageRequest.Builder>().data(any()) }
        verify(exactly = 0) { imageRequestBuilder.target(any<ImageView>()) }
        verify(exactly = 0) { imageRequestBuilder.size(any(), any()) }
        verify(exactly = 0) { imageRequestBuilder.build() }
        verify(exactly = 0) { imageLoader.enqueue(any()) }
        verify(exactly = 0) { targetView.getContext() }
        verify(exactly = 0) { targetView.getMeasuredWidth() }
        verify(exactly = 0) { targetView.getMeasuredHeight() }
    }


    @Test
    fun testWidthZero() {
        every { targetView.getMeasuredWidth() }.returns(0)

        backgroundImageLoaderUseCase.invoke(targetView, "test")

        verify(exactly = 1) { anyConstructed<ImageRequest.Builder>().data(any()) }
        verify(exactly = 1) { imageRequestBuilder.target(any<ImageView>()) }
        verify(exactly = 0) { imageRequestBuilder.size(any(), any()) }
        verify(exactly = 1) { imageRequestBuilder.build() }
        verify(exactly = 1) { imageLoader.enqueue(any()) }
        verify(atLeast = 1) { targetView.getContext() }
        verify(atLeast = 1) { targetView.getMeasuredWidth() }
        verify(exactly = 0) { targetView.getMeasuredHeight() }
    }

    @Test
    fun testHeightZero() {
        every { targetView.getMeasuredHeight() }.returns(0)

        backgroundImageLoaderUseCase.invoke(targetView, "test")

        verify(exactly = 1) { anyConstructed<ImageRequest.Builder>().data(any()) }
        verify(exactly = 1) { imageRequestBuilder.target(any<ImageView>()) }
        verify(exactly = 0) { imageRequestBuilder.size(any(), any()) }
        verify(exactly = 1) { imageRequestBuilder.build() }
        verify(exactly = 1) { imageLoader.enqueue(any()) }
        verify(atLeast = 1) { targetView.getContext() }
        verify(atLeast = 1) { targetView.getMeasuredWidth() }
        verify(exactly = 1) { targetView.getMeasuredHeight() }
    }

}