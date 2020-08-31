/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import jp.toastkid.article_viewer.article.list.SearchResult

/**
 * @author toastkidjp
 */
@Dao
interface ArticleRepository {

    @Query("SELECT * FROM article ORDER BY lastModified DESC")
    fun getAllWithContent(): List<Article>

    @Query("SELECT id, title, lastModified, length FROM article ORDER BY lastModified DESC")
    fun orderByLastModified(): PagingSource<Int, SearchResult>

    @Query("SELECT id, title, lastModified, length FROM article ORDER BY title DESC")
    fun orderByName(): PagingSource<Int, SearchResult>

    @Query("SELECT id, title, lastModified, length FROM article ORDER BY length, lastModified ASC")
    fun orderByLength(): PagingSource<Int, SearchResult>

    @Query("SELECT id, title, lastModified, length FROM article WHERE title LIKE :title ORDER BY lastModified DESC")
    fun filter(title: String): PagingSource<Int, SearchResult>

    @Query("SELECT contentText FROM article WHERE title = :title LIMIT 1")
    fun findContentByTitle(title: String): String?

    @Query("SELECT * FROM article WHERE title LIKE :title LIMIT 1")
    fun findFirst(title: String): Article?

    @Query("SELECT article.id, article.title, article.lastModified, article.length FROM article JOIN articleFts ON (article.id = articleFts.docid) WHERE articleFts MATCH :query")
    fun search(query: String): PagingSource<Int, SearchResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: Article)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: Collection<Article>)

    @Query("DELETE FROM article WHERE article.id = :id")
    fun delete(id: Int)

    @Query("DELETE FROM article")
    fun deleteAll()

    @Query("SELECT article.id, article.title, article.lastModified, article.length FROM article WHERE article.id IN (:articleIds)")
    fun findByIds(articleIds: List<Int>): PagingSource<Int, SearchResult>

    @Query("SELECT COUNT(article.title) FROM article WHERE article.title = :title")
    fun exists(title: String): Int

}