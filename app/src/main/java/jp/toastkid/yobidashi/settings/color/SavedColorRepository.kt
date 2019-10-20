/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.color

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

/**
 * @author toastkidjp
 */
@Dao
interface SavedColorRepository {

    @Insert
    fun add(item: SavedColor)

    @Query("SELECT * FROM SavedColor")
    fun findAll(): List<SavedColor>

    @Delete
    fun delete(item: SavedColor)

    @Query("DELETE FROM SavedColor")
    fun deleteAll()
}