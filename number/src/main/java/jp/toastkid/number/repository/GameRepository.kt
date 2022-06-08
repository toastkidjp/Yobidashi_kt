/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.number.repository

import jp.toastkid.number.model.NumberPlaceGame
import java.io.File

interface GameRepository {

    fun save(file: File, game: NumberPlaceGame)

    fun load(file: File): NumberPlaceGame?

    fun delete(file: File?): Boolean

}