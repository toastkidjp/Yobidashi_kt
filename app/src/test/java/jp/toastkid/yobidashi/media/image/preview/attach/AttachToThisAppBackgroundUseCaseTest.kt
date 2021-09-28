/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.preview.attach

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
import jp.toastkid.lib.window.WindowRectCalculatorCompat
import jp.toastkid.yobidashi.settings.background.load.ImageStoreService
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
    private lateinit var imageStoreServiceFactory: (Context) -> ImageStoreService

    @MockK
    private lateinit var windowRectCalculatorCompat: WindowRectCalculatorCompat

    @MockK
    private lateinit var imageStoreService: ImageStoreService

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
        every { imageStoreServiceFactory.invoke(any()) }.returns(imageStoreService)
        every { imageStoreService.invoke(any(), any(), any()) }.just(Runs)
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
        verify(exactly = 1) { imageStoreServiceFactory.invoke(any()) }
        verify(exactly = 1) { imageStoreService.invoke(any(), any(), any()) }
    }

}