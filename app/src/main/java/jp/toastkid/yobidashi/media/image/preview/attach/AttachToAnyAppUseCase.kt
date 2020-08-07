/*
 * Copyright (c) 2019 toastkidjp.
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
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.libs.ImageCache

/**
 * @author toastkidjp
 */
class AttachToAnyAppUseCase(private val activityStarter: (Intent) -> Unit) {

    operator fun invoke(context: Context, bitmap: Bitmap) {
        val intent = Intent(Intent.ACTION_ATTACH_DATA)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        val uri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                ImageCache().saveBitmap(context.cacheDir, bitmap).absoluteFile
        )
        intent.setDataAndType(uri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        activityStarter(Intent.createChooser(intent, "Set as:"))
    }

}