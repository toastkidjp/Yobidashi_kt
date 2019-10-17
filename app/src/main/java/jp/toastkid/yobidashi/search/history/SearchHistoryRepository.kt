/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.history

import androidx.room.*

/**
 * @author toastkidjp
 */
@Dao
interface SearchHistoryRepository {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: SearchHistory)

    @Query("SELECT * FROM SearchHistory WHERE `query` LIKE :keyword ORDER BY timestamp DESC LIMIT 5")
    fun select(keyword: String): List<SearchHistory>

    @Query("SELECT * FROM SearchHistory")
    fun findAll(): List<SearchHistory>

    @Query("SELECT * FROM SearchHistory ORDER BY timestamp DESC LIMIT 5")
    fun findLast5(): List<SearchHistory>

    @Delete
    fun delete(searchHistory: SearchHistory)

    @Query("DELETE FROM SearchHistory")
    fun deleteAll()
}