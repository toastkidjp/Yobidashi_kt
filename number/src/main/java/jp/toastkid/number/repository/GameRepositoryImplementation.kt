/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number.repository

import jp.toastkid.number.model.NumberPlaceGame
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class GameRepositoryImplementation : GameRepository {

    override fun save(file: File, game: NumberPlaceGame) {
        val encodeToString = Json.encodeToString(game)
        val printWriter = file.printWriter()
        printWriter.println(encodeToString)
        printWriter.flush()
    }

    override fun load(file: File): NumberPlaceGame? {
        if (file.exists().not()) {
            return null
        }

        val string = file.bufferedReader().readText()
        if (string.isBlank()) {
            return null
        }
        return Json.decodeFromString(string)
    }

    override fun delete(file: File?): Boolean {
        return file?.delete() ?: false
    }

}