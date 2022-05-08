/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.editor.load

import android.content.Context
import android.os.Environment
import jp.toastkid.lib.io.TextFileFilter
import java.io.File

class StorageFilesFinder(private val textFileFilter: TextFileFilter = TextFileFilter()) {

    operator fun invoke(context: Context?): Array<File>? =
        context
            ?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?.listFiles(textFileFilter)

}