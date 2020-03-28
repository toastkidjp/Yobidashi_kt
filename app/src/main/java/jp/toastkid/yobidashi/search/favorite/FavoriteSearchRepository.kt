/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.favorite

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * @author toastkidjp
 */
@Dao
interface FavoriteSearchRepository {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: FavoriteSearch)

    @Query("SELECT * FROM FavoriteSearch WHERE query LIKE :keyword ORDER BY id DESC LIMIT 5")
    fun select(keyword: String): List<FavoriteSearch>

    @Query("SELECT * FROM FavoriteSearch")
    fun findAll(): List<FavoriteSearch>

    @Query("SELECT * FROM FavoriteSearch ORDER BY id DESC LIMIT :count")
    fun find(count: Int): List<FavoriteSearch>

    @Delete
    fun delete(favoriteSearch: FavoriteSearch)

    @Query("DELETE FROM FavoriteSearch")
    fun deleteAll()
}