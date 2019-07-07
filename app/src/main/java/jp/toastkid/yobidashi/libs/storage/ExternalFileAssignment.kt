/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.storage

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

/**
 * @author toastkidjp
 */
class ExternalFileAssignment {

    fun assignFile(context: Context, fileName: String): File {
        val externalFilesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        } else {
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        }

        if (!externalFilesDir.exists()) {
            externalFilesDir.mkdirs()
        }
        return File(externalFilesDir, fileName)
    }
}