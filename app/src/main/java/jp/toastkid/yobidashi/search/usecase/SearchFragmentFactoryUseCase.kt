/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.usecase

import jp.toastkid.lib.Urls
import jp.toastkid.search.SearchQueryExtractor
import jp.toastkid.yobidashi.search.SearchFragment

class SearchFragmentFactoryUseCase {

    operator fun invoke(titleAndUrl: Pair<String?, String?>): SearchFragment {
        val currentTitle = titleAndUrl.first
        val currentUrl = titleAndUrl.second
        val query = SearchQueryExtractor().invoke(currentUrl)
        val makeIntent = if (query.isNullOrEmpty() || Urls.isValidUrl(query)) {
            SearchFragment.makeWith(currentTitle, currentUrl)
        } else {
            SearchFragment.makeWithQuery(query, currentTitle, currentUrl)
        }
        return makeIntent
    }

}