/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number.factory

import jp.toastkid.lib.preference.PreferenceApplier
import java.io.File

class GameFileProvider {

    operator fun invoke(filesDir: File, preferenceApplier: PreferenceApplier): File? {
        if (preferenceApplier.lastNumberPlaceGamePath().isNullOrBlank()) {
            val dir = File(filesDir, FOLDER_NAME)
            if (dir.exists().not()) {
                dir.mkdirs()
            }

            val file = File(dir, FIXED_FILE_NAME)

            if (file.exists().not()) {
                file.createNewFile()
            }
            preferenceApplier.setLastNumberPlaceGamePath(file.name)
        }

        val pathname = preferenceApplier.lastNumberPlaceGamePath() ?: return null
        return File(filesDir, "$FOLDER_NAME/$pathname")
    }

    companion object {

        private const val FOLDER_NAME = "number/place/games"

        private const val FIXED_FILE_NAME = "saved_game"

    }
}