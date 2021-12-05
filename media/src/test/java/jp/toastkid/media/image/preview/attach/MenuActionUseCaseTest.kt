/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.media.image.preview.attach

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.fragment.app.DialogFragment
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
import jp.toastkid.media.image.preview.detail.ImageDetailFragment
import org.junit.After
import org.junit.Before
import org.junit.Test

class MenuActionUseCaseTest {

    @InjectMockKs
    private lateinit var menuActionUseCase: MenuActionUseCase

    @MockK
    private lateinit var attachToAnyAppUseCase: AttachToAnyAppUseCase

    @MockK
    private lateinit var attachToThisAppBackgroundUseCase: AttachToThisAppBackgroundUseCase

    @MockK
    private lateinit var uriSupplier: () -> Uri

    @MockK
    private lateinit var bitmapSupplier: () -> Bitmap

    @MockK
    private lateinit var showDialog: (DialogFragment) -> Unit

    @MockK
    private lateinit var view: View

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { attachToAnyAppUseCase.invoke(any(), any()) }.just(Runs)
        every { attachToThisAppBackgroundUseCase.invoke(any(), any(), any()) }.just(Runs)
        every { uriSupplier.invoke() }.returns(mockk())
        every { bitmapSupplier.invoke() }.returns(mockk())
        every { showDialog.invoke(any()) }.just(Runs)
        every { view.getContext() }.returns(mockk())

        mockkObject(ImageDetailFragment)
        every { ImageDetailFragment.withImageUri(any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testThisApp() {
        menuActionUseCase.thisApp(view)

        verify (exactly = 1) { view.getContext() }
        verify (exactly = 0) { attachToAnyAppUseCase.invoke(any(), any()) }
        verify (exactly = 1) { attachToThisAppBackgroundUseCase.invoke(any(), any(), any()) }
        verify (exactly = 1) { uriSupplier.invoke() }
        verify (exactly = 1) { bitmapSupplier.invoke() }
        verify (exactly = 0) { showDialog.invoke(any()) }
    }

    @Test
    fun testOtherApp() {
        menuActionUseCase.otherApp(view)

        verify (exactly = 1) { view.getContext() }
        verify (exactly = 1) { attachToAnyAppUseCase.invoke(any(), any()) }
        verify (exactly = 0) { attachToThisAppBackgroundUseCase.invoke(any(), any(), any()) }
        verify (exactly = 0) { uriSupplier.invoke() }
        verify (exactly = 1) { bitmapSupplier.invoke() }
        verify (exactly = 0) { showDialog.invoke(any()) }
    }

    @Test
    fun testDetail() {
        menuActionUseCase.detail()

        verify (exactly = 0) { view.getContext() }
        verify (exactly = 0) { attachToAnyAppUseCase.invoke(any(), any()) }
        verify (exactly = 1) { uriSupplier.invoke() }
        verify (exactly = 0) { bitmapSupplier.invoke() }
        verify (exactly = 1) { showDialog.invoke(any()) }
    }

}