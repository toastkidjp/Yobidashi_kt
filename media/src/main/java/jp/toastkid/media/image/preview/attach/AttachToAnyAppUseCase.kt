/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.media.image.preview.attach

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import jp.toastkid.lib.image.ImageCache

/**
 * @author toastkidjp
 */
class AttachToAnyAppUseCase(
    private val activityStarter: (Intent) -> Unit,
    private val intentFactory: () -> Intent = { Intent(Intent.ACTION_ATTACH_DATA) }
) {

    operator fun invoke(context: Context, bitmap: Bitmap) {
        val intent = intentFactory()
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        val uri = FileProvider.getUriForFile(
                context,
                AUTHORITY,
                ImageCache().saveBitmap(context.cacheDir, bitmap).absoluteFile
        )
        intent.setDataAndType(uri, MIME_TYPE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        activityStarter(Intent.createChooser(intent, CHOOSER_TITLE))
    }

    companion object {
        private const val AUTHORITY = "jp.toastkid.yobidashi.fileprovider"

        private const val MIME_TYPE = "image/*"

        private const val CHOOSER_TITLE = "Set as:"
    }

}
