/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import jp.toastkid.yobidashi.BuildConfig
import java.io.File

/**
 * @author toastkidjp
 */
class ImageEditChooserFactory {

    operator fun invoke(context: Context, path: String?): Intent {
        val intent = Intent(Intent.ACTION_EDIT)

        val uriForFile = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                File(path)
        )
        intent.setDataAndType(uriForFile, "image/*")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        return Intent.createChooser(intent, "File edit")
    }
}