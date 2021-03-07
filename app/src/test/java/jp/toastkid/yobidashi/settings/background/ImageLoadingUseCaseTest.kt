/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.background

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageLoadingUseCaseTest {

    private lateinit var imageLoadingUseCase: ImageLoadingUseCase

    @MockK
    private lateinit var contentView: View

    @MockK
    private lateinit var arguments: Bundle

    @MockK
    private lateinit var imageView: ImageView

    @MockK
    private lateinit var imageRequestBuilder: ImageRequest.Builder

    @MockK
    private lateinit var imageLoader: ImageLoader

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        imageLoadingUseCase = ImageLoadingUseCase()

        every { contentView.findViewById<ImageView>(any()) }.returns(imageView)
        every { imageView.getContext() }.returns(context)

        mockkConstructor(ImageRequest.Builder::class)
        every { anyConstructed<ImageRequest.Builder>().data(any()) }.returns(imageRequestBuilder)
        every { imageRequestBuilder.target(any<ImageView>()) }.returns(imageRequestBuilder)
        every { imageRequestBuilder.build() }.returns(mockk())
        mockkObject(Coil)
        every { Coil.imageLoader(any()) }.returns(imageLoader)
        every { imageLoader.enqueue(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun test() {
        every { arguments.containsKey(any()) }.returns(true)
        every { arguments.getParcelable<Bitmap>(any()) }.returns(mockk())

        imageLoadingUseCase.invoke(contentView, arguments)

        verify(exactly = 1) { arguments.containsKey(any()) }
        verify(exactly = 1) { arguments.getParcelable<Bitmap>(any()) }
        verify(exactly = 1) { contentView.findViewById<ImageView>(any()) }
        verify(atLeast = 1) { imageView.getContext() }

        verify(exactly = 1) { anyConstructed<ImageRequest.Builder>().data(any()) }
        verify(exactly = 1) { imageRequestBuilder.target(any<ImageView>()) }
        verify(exactly = 1) { imageRequestBuilder.build() }
        verify(exactly = 1) { imageLoader.enqueue(any()) }
    }

    @Test
    fun testNotContainsKey() {
        every { arguments.containsKey(any()) }.returns(false)
        every { arguments.getParcelable<Bitmap>(any()) }.returns(mockk())

        imageLoadingUseCase.invoke(contentView, arguments)

        verify(atLeast = 1) { arguments.containsKey(any()) }
        verify(exactly = 0) { arguments.getParcelable<Bitmap>(any()) }
        verify(exactly = 1) { contentView.findViewById<ImageView>(any()) }
        verify(exactly = 0) { imageView.getContext() }

        verify(exactly = 0) { anyConstructed<ImageRequest.Builder>().data(any()) }
        verify(exactly = 0) { imageRequestBuilder.target(any<ImageView>()) }
        verify(exactly = 0) { imageRequestBuilder.build() }
        verify(exactly = 0) { imageLoader.enqueue(any()) }
    }

    @Test
    fun testUrlCase() {
        every { arguments.containsKey("image") }.returns(false)
        every { arguments.containsKey("imageUrl") }.returns(true)
        every { arguments.getString(any()) }.returns("test")

        imageLoadingUseCase.invoke(contentView, arguments)

        verify(atLeast = 1) { arguments.containsKey(any()) }
        verify(exactly = 1) { arguments.getString(any()) }
        verify(exactly = 1) { contentView.findViewById<ImageView>(any()) }
        verify(atLeast = 1) { imageView.getContext() }

        verify(exactly = 1) { anyConstructed<ImageRequest.Builder>().data(any()) }
        verify(exactly = 1) { imageRequestBuilder.target(any<ImageView>()) }
        verify(exactly = 1) { imageRequestBuilder.build() }
        verify(exactly = 1) { imageLoader.enqueue(any()) }
    }

}