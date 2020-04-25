/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.bookmark.model

import androidx.room.*

/**
 * @author toastkidjp
 */
@Dao
interface BookmarkRepository {

    @Query("SELECT * FROM Bookmark")
    fun all(): List<Bookmark>

    @Query("SELECT * FROM Bookmark WHERE parent = :folderName")
    fun findByParent(folderName: String): List<Bookmark>

    @Query("SELECT * FROM Bookmark WHERE title LIKE :query")
    fun search(query: String): List<Bookmark>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(bookmark: Bookmark)

    @Delete
    fun delete(bookmark: Bookmark)

    @Query("DELETE FROM Bookmark")
    fun clear()

}