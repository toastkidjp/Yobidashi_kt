/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.media.image.preview.attach

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.yobidashi.libs.ImageCache
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * @author toastkidjp
 */
class AttachToAnyAppUseCaseTest {

    @InjectMockKs
    private lateinit var attachToAnyAppUseCase: AttachToAnyAppUseCase

    @MockK
    private lateinit var activityStarter: (Intent) -> Unit

    @MockK
    private lateinit var intentFactory: () -> Intent

    @MockK
    private lateinit var intent: Intent

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var bitmap: Bitmap

    @MockK
    private lateinit var file: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { activityStarter.invoke(any()) }.returns(Unit)
        every { intentFactory.invoke() }.returns(intent)
        every { intent.addCategory(Intent.CATEGORY_DEFAULT) }.returns(intent)
        every { intent.setDataAndType(any(), any()) }.returns(intent)
        every { intent.addFlags(any()) }.returns(intent)
        every { context.getCacheDir() }.returns(mockk())

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) }.returns(mockk())

        mockkStatic(Intent::class)
        every { Intent.createChooser(any(), any()) }.returns(mockk())

        mockkConstructor(ImageCache::class)
        every { anyConstructed<ImageCache>().saveBitmap(any(), any()) }.returns(file)
        every { file.getAbsoluteFile() }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        attachToAnyAppUseCase(context, bitmap)

        verify(exactly = 1) { activityStarter.invoke(any()) }
        verify(exactly = 1) { intentFactory.invoke() }
        verify(exactly = 1) { intent.addCategory(Intent.CATEGORY_DEFAULT) }
        verify(exactly = 1) { intent.setDataAndType(any(), any()) }
        verify(exactly = 1) { intent.addFlags(any()) }
        verify(exactly = 1) { context.getCacheDir() }
        verify(exactly = 1) { FileProvider.getUriForFile(any(), any(), any()) }
        verify(exactly = 1) { Intent.createChooser(any(), any()) }
        verify(exactly = 1) { anyConstructed<ImageCache>().saveBitmap(any(), any()) }
        verify(exactly = 1) { file.getAbsoluteFile() }
    }

}