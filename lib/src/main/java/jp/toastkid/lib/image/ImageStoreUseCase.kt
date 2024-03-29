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
import android.os.Build
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.StorageWrapper
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.UUID

/**
 * @author toastkidjp
 */
class ImageStoreUseCase(
    private val filesDir: StorageWrapper,
    private val preferenceApplier: PreferenceApplier,
    private val bitmapScaling: BitmapScaling = BitmapScaling()
) {

    /**
     * Store image file.
     *
     * @param image
     * @param uri
     * @param displaySize
     *
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    operator fun invoke(image: Bitmap, uri: Uri, displaySize: Rect, fileActionAfterStored: (File) -> Unit = {}) {
        val output = filesDir.assignNewFile(uri.lastPathSegment ?: UUID.randomUUID().toString())
        preferenceApplier.backgroundImagePath = output.path

        val fileOutputStream = FileOutputStream(output)
        val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSLESS
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
        bitmapScaling(image, displaySize.width().toDouble(), displaySize.height().toDouble())
                .compress(compressFormat, 100, fileOutputStream)
        fileOutputStream.close()

        fileActionAfterStored(output)
    }

}