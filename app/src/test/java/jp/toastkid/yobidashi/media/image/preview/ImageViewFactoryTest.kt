/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.preview

import android.content.Context
import android.content.res.Resources
import com.github.chrisbanes.photoview.PhotoView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImageViewFactoryTest {

    private lateinit var imageViewFactory: ImageViewFactory

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var photoView: PhotoView

    @Before
    fun setUp() {
        imageViewFactory = ImageViewFactory({ photoView })

        MockKAnnotations.init(this)
        every { context.getResources() }.returns(resources)
        every { resources.getDimensionPixelSize(any()) }.returns(10)

        every { photoView.setLayoutParams(any()) }.answers { Unit }
        every { photoView.setPadding(any(), any(), any(), any()) }.answers { Unit }
        every { photoView.setMaximumScale(any()) }.answers { Unit }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        imageViewFactory.invoke(context)

        verify(exactly = 1) { context.getResources() }
        verify(exactly = 1) { resources.getDimensionPixelSize(any()) }
        verify(exactly = 1) { photoView.setLayoutParams(any()) }
        verify(exactly = 1) { photoView.setPadding(any(), any(), any(), any()) }
        verify(exactly = 1) { photoView.setMaximumScale(any()) }
    }

}