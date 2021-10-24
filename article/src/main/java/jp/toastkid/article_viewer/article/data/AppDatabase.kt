/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.data

import android.content.Context
import androidx.annotation.VisibleForTesting
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
@Database(
    entities = [Article::class, ArticleFts::class, Bookmark::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun articleRepository(): ArticleRepository

    abstract fun bookmarkRepository(): BookmarkRepository

    companion object {

        private var instance: AppDatabase? = null

        fun find(activityContext: Context): AppDatabase {
            instance = synchronized(this) {
                if (instance != null) instance
                else makeAppDatabase(activityContext)
            }

            return instance ?: makeAppDatabase(activityContext)
        }

        private fun makeAppDatabase(activityContext: Context) = Room.databaseBuilder(
            activityContext.applicationContext,
            AppDatabase::class.java,
            "article_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        @VisibleForTesting
        fun clearInstance() {
            instance = null
        }

    }
}