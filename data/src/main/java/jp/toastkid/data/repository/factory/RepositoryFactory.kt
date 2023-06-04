/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.data.repository.factory

import android.content.Context
import jp.toastkid.data.DatabaseFinder
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchRepository
import jp.toastkid.yobidashi.search.history.SearchHistoryRepository
import jp.toastkid.yobidashi.settings.color.SavedColorRepository

class RepositoryFactory {

    private val databaseFinder: DatabaseFinder = DatabaseFinder()

    fun favoriteSearchRepository(context: Context): FavoriteSearchRepository =
        databaseFinder.invoke(context).favoriteSearchRepository()

    fun searchHistoryRepository(context: Context): SearchHistoryRepository =
        databaseFinder.invoke(context).searchHistoryRepository()

    fun savedColorRepository(context: Context): SavedColorRepository =
        databaseFinder.invoke(context).savedColorRepository()

    fun viewHistoryRepository(context: Context): ViewHistoryRepository =
        databaseFinder.invoke(context).viewHistoryRepository()

    fun bookmarkRepository(context: Context): BookmarkRepository =
        databaseFinder.invoke(context).bookmarkRepository()

}