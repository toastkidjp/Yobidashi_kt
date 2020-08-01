/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.background.load

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.view.Display
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.StorageWrapper
import jp.toastkid.yobidashi.libs.BitmapScaling
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * @author toastkidjp
 */
class ImageStoreService(
        private val filesDir: StorageWrapper,
        private val preferenceApplier: PreferenceApplier
) {

    /**
     * Store image file.
     *
     * @param context
     * @param image
     *
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    operator fun invoke(image: Bitmap, uri: Uri, display: Display?) {
        val output = filesDir.assignNewFile(uri)
        preferenceApplier.backgroundImagePath = output.path

        val size = getDisplayScale(display)
        val fileOutputStream = FileOutputStream(output)
        BitmapScaling(image, size.width().toDouble(), size.height().toDouble())
                .compress(Bitmap.CompressFormat.WEBP, 100, fileOutputStream)
        fileOutputStream.close()
    }

    private fun getDisplayScale(display: Display?): Rect {
        val size = Rect()
        display?.getRectSize(size)
        return size
    }

}