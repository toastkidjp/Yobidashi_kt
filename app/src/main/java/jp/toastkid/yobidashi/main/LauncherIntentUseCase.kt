/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import jp.toastkid.lib.FileExtractorFromUri
import jp.toastkid.lib.Urls
import jp.toastkid.yobidashi.barcode.BarcodeReaderFragment
import jp.toastkid.yobidashi.browser.bookmark.BookmarkFragment
import jp.toastkid.yobidashi.launcher.LauncherFragment
import jp.toastkid.yobidashi.search.SearchFragment
import jp.toastkid.yobidashi.search.favorite.AddingFavoriteSearchService
import jp.toastkid.yobidashi.settings.SettingFragment

/**
 * @author toastkidjp
 */
class LauncherIntentUseCase(
        private val randomWikipediaUseCase: RandomWikipediaUseCase,
        private val openNewWebTab: (Uri) -> Unit,
        private val openEditorTab: (Uri) -> Unit,
        private val search: (String, String) -> Unit,
        private val searchCategoryFinder: () -> String,
        private val replaceFragment: (Class<out Fragment>) -> Unit,
        private val elseCaseUseCase: ElseCaseUseCase
) {
    
    operator fun invoke(intent: Intent) {
        if (intent.getBooleanExtra("random_wikipedia", false)) {
            randomWikipediaUseCase.invoke()
            return
        }

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                val uri = intent.data ?: return
                when (uri.scheme) {
                    "content" -> openEditorTab(uri)
                    else -> openNewWebTab(uri)
                }
                return
            }
            Intent.ACTION_SEND -> {
                intent.extras?.getCharSequence(Intent.EXTRA_TEXT)?.also {
                    val query = it.toString()
                    if (Urls.isInvalidUrl(query)) {
                        search(searchCategoryFinder(), query)
                        return
                    }
                    openNewWebTab(query.toUri())
                }
                return
            }
            Intent.ACTION_WEB_SEARCH -> {
                val category = if (intent.hasExtra(AddingFavoriteSearchService.EXTRA_KEY_CATEGORY)) {
                    intent.getStringExtra(AddingFavoriteSearchService.EXTRA_KEY_CATEGORY)
                } else {
                    searchCategoryFinder()
                }
                search(category, intent.getStringExtra(SearchManager.QUERY) ?: "")
                return
            }
            BOOKMARK -> {
                replaceFragment(BookmarkFragment::class.java)
            }
            APP_LAUNCHER -> {
                replaceFragment(LauncherFragment::class.java)
            }
            BARCODE_READER -> {
                replaceFragment(BarcodeReaderFragment::class.java)
            }
            SEARCH -> {
                replaceFragment(SearchFragment::class.java)
            }
            SETTING -> {
                replaceFragment(SettingFragment::class.java)
            }
            else -> {
                elseCaseUseCase()
            }
        }
    }
}