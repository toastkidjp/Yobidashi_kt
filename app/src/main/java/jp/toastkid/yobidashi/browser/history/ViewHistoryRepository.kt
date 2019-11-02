/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.history

import androidx.room.*

/**
 * @author toastkidjp
 */
@Dao
interface ViewHistoryRepository {

    @Query("SELECT * FROM ViewHistory WHERE url LIKE :query ORDER BY _id DESC LIMIT :limit")
    fun search(query: String, limit: Int): List<ViewHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(viewHistory: ViewHistory)

    @Delete
    fun delete(viewHistory: ViewHistory)

    @Query("DELETE FROM ViewHistory")
    fun deleteAll()

    @Query("SELECT * FROM ViewHistory ORDER BY _id DESC")
    fun reversed(): List<ViewHistory>
}