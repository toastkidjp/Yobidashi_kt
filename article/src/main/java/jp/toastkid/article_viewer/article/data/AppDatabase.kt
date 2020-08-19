/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.article.ArticleFts
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.bookmark.Bookmark
import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository

/**
 * @author toastkidjp
 */
@Database(entities = [Article::class, ArticleFts::class, Bookmark::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun articleRepository(): ArticleRepository

    abstract fun bookmarkRepository(): BookmarkRepository

    companion object {
        fun find(activityContext: Context): AppDatabase {
            return Room.databaseBuilder(
                    activityContext.applicationContext,
                    AppDatabase::class.java,
                    "article_db"
            )
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}