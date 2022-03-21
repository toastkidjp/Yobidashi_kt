/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.preview.attach

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.WindowManager
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.image.ImageStoreUseCase
import jp.toastkid.lib.window.WindowRectCalculatorCompat
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class AttachToThisAppBackgroundUseCaseTest {

    @InjectMockKs
    private lateinit var attachToThisAppBackgroundUseCase: AttachToThisAppBackgroundUseCase

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var imageStoreUseCaseFactory: (Context) -> ImageStoreUseCase

    @MockK
    private lateinit var windowRectCalculatorCompat: WindowRectCalculatorCompat

    @MockK
    private lateinit var imageStoreUseCase: ImageStoreUseCase

    @MockK
    private lateinit var context: Activity

    @MockK
    private lateinit var uri: Uri

    @MockK
    private lateinit var image: Bitmap

    @MockK
    private lateinit var windowManager: WindowManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { contentViewModel.refresh() }.just(Runs)
        every { contentViewModel.snackShort(any<Int>()) }.just(Runs)
        every { imageStoreUseCaseFactory.invoke(any()) }.returns(imageStoreUseCase)
        every { imageStoreUseCase.invoke(any(), any(), any()) }.just(Runs)
        every { windowRectCalculatorCompat.invoke(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        attachToThisAppBackgroundUseCase.invoke(context, uri, image)

        verify(exactly = 1) { contentViewModel.refresh() }
        verify(exactly = 1) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 1) { windowRectCalculatorCompat.invoke(any()) }
        verify(exactly = 1) { imageStoreUseCaseFactory.invoke(any()) }
        verify(exactly = 1) { imageStoreUseCase.invoke(any(), any(), any()) }
    }

    @Test
    fun testCannotWindowRectCase() {
        every { windowRectCalculatorCompat.invoke(any()) }.returns(null)

        attachToThisAppBackgroundUseCase.invoke(context, uri, image)

        verify(exactly = 1) { windowRectCalculatorCompat.invoke(any()) }
        verify(exactly = 0) { contentViewModel.refresh() }
        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { imageStoreUseCaseFactory.invoke(any()) }
        verify(exactly = 0) { imageStoreUseCase.invoke(any(), any(), any()) }
    }

}