/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.image

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

/**
 * @author toastkidjp
 */
class ImageStoreServiceTest {

    @InjectMockKs
    private lateinit var imageStoreService: ImageStoreService

    @MockK
    private lateinit var filesDir: FilesDir

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var bitmap: Bitmap

    @MockK
    private lateinit var uri: Uri

    @MockK
    private lateinit var display: Rect

    @MockK
    private lateinit var scaledBitmap: Bitmap

    @MockK
    private lateinit var bitmapScaling: BitmapScaling

    private lateinit var file: File

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        file = spyk(File.createTempFile("test", "webp"))
        every { filesDir.assignNewFile(any<String>()) }.answers { file }
        every { preferenceApplier.backgroundImagePath = any() }.answers { Unit }

        mockkConstructor(FileOutputStream::class)
        every { anyConstructed<FileOutputStream>().close() }.answers { Unit }

        every { scaledBitmap.compress(any(), any(), any()) }.answers { true }

        every { bitmapScaling.invoke(any(), any(), any()) }.answers { scaledBitmap }

        every { uri.getLastPathSegment() }.returns("last")

        every { display.width() }.returns(1024)
        every { display.height() }.returns(1920)
    }

    @After
    fun tearDown() {
        unmockkAll()
        file.delete()
    }

    @Test
    fun test() {
        imageStoreService.invoke(bitmap, uri, display)

        verify(exactly = 2) { file.getPath() }
        verify(exactly = 1) { filesDir.assignNewFile(any<String>()) }
        verify(exactly = 1) { anyConstructed<FileOutputStream>().close() }
        verify(exactly = 1) { bitmapScaling.invoke(any(), any(), any()) }
        verify(exactly = 1) { uri.getLastPathSegment() }
    }

}