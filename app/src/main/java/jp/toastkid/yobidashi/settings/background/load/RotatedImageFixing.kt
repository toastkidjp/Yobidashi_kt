/*
 * Copyright (c) 2019 toastkidjp.
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
import androidx.exifinterface.media.ExifInterface
import java.io.FileDescriptor

/**
 * @author toastkidjp
 */
class RotatedImageFixing(
    private val exifInterfaceFactory: (FileDescriptor) -> ExifInterface = { ExifInterface(it) }
) {

    operator fun invoke(contentResolver: ContentResolver, bitmap: Bitmap?, imageUri: Uri): Bitmap? {
        if (bitmap == null) {
            return null
        }

        val openFileDescriptor = contentResolver.openFileDescriptor(imageUri, "r") ?: return bitmap
        val exifInterface = exifInterfaceFactory(openFileDescriptor.fileDescriptor)
        val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270F)
            else -> bitmap
        }
    }

    private fun rotate(bitmap: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
        )
        bitmap.recycle()
        return rotatedBitmap
    }

}