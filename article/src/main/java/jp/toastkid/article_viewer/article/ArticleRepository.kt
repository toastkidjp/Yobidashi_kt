/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article

import androidx.room.*
import jp.toastkid.article_viewer.article.list.SearchResult

/**
 * @author toastkidjp
 */
@Dao
interface ArticleRepository {

    @Query("SELECT * FROM article ORDER BY lastModified DESC")
    fun getAllWithContent(): List<Article>

    @Query("SELECT title, lastModified, length FROM article ORDER BY lastModified DESC LIMIT 500")
    fun getAll(): List<SearchResult>

    @Query("SELECT title, lastModified, length FROM article WHERE title LIKE :title ORDER BY lastModified DESC")
    fun filter(title: String): List<SearchResult>

    @Query("SELECT contentText FROM article WHERE title = :title LIMIT 1")
    fun findContentByTitle(title: String): String?

    @Query("SELECT * FROM article WHERE title LIKE :title LIMIT 1")
    fun findFirst(title: String): Article?

    @Query("SELECT article.title, article.lastModified, article.length FROM article JOIN articleFts ON (article.id = articleFts.docid) WHERE articleFts MATCH :query")
    fun search(query: String): List<SearchResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: Article)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: Collection<Article>)

    @Query("DELETE FROM article")
    fun deleteAll()
}