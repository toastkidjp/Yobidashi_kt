/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.storage

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * @author toastkidjp
 */
class ExternalFileAssignment {

    operator fun invoke(context: Context, fileName: String): File {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        if (externalFilesDir?.exists() != false) {
            externalFilesDir?.mkdirs()
        }
        return File(externalFilesDir, fileName)
    }
}