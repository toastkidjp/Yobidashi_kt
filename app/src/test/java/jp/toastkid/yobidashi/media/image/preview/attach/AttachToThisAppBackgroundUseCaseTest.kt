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
import io.mockk.mockkObject
import io.mockk.unmockkAll
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.yobidashi.settings.background.load.ImageStoreService
import jp.toastkid.yobidashi.settings.fragment.DisplayingSettingFragment
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
        every { context.windowManager }.returns(windowManager)
        every { windowManager.defaultDisplay }.returns(mockk())
        every { imageStoreServiceFactory.invoke(any()) }.returns(imageStoreService)
        every { imageStoreService.invoke(any(), any(), any()) }.just(Runs)

        mockkObject(DisplayingSettingFragment)
        every { DisplayingSettingFragment.getBackgroundDirectory() }.returns("test")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        attachToThisAppBackgroundUseCase.invoke(context, uri, image)
    }

}