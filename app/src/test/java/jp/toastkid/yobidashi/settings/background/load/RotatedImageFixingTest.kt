/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.background.load

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.exifinterface.media.ExifInterface
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.FileDescriptor

class RotatedImageFixingTest {

    @InjectMockKs
    private lateinit var rotatedImageFixing: RotatedImageFixing

    @MockK
    private lateinit var exifInterfaceFactory: (FileDescriptor) -> ExifInterface

    @MockK
    private lateinit var contentResolver: ContentResolver

    @MockK
    private lateinit var bitmap: Bitmap

    @MockK
    private lateinit var imageUri: Uri

    @MockK
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor

    @MockK
    private lateinit var exifInterface: ExifInterface

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { contentResolver.openFileDescriptor(any(), any()) }.returns(parcelFileDescriptor)
        every { parcelFileDescriptor.fileDescriptor }.returns(mockk())
        every { bitmap.width }.returns(120)
        every { bitmap.height }.returns(1920)
        every { bitmap.recycle() }.just(Runs)
        every { exifInterfaceFactory.invoke(any()) }.returns(exifInterface)
        every { exifInterface.getAttributeInt(any(), any()) }
            .returns(ExifInterface.ORIENTATION_ROTATE_90)

        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any<Bitmap>(), any(), any(), any(), any(), any(), any()) }
            .returns(bitmap)

        mockkConstructor(Matrix::class)
        every { anyConstructed<Matrix>().postRotate(any()) }.returns(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        rotatedImageFixing.invoke(contentResolver, bitmap, imageUri)

        verify(exactly = 1) { Bitmap.createBitmap(any<Bitmap>(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun test() {
        rotatedImageFixing.invoke(contentResolver, null, imageUri)

        verify(exactly = 0) { Bitmap.createBitmap(any<Bitmap>(), any(), any(), any(), any(), any(), any()) }
    }

}