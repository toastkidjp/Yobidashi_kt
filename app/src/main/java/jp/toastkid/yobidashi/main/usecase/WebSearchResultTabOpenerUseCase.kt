/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.usecase

import android.net.Uri
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.UrlFactory

class WebSearchResultTabOpenerUseCase(
    private val preferenceApplier: PreferenceApplier,
    private val openNewWebTab: (Uri) -> Unit,
    private val urlFactory: UrlFactory = UrlFactory()
) {

    operator fun invoke(query: String) {
        val validatedUrl = Urls.isValidUrl(query)
        if (validatedUrl) {
            openNewWebTab(Uri.parse(query))
            return
        }

        val category = preferenceApplier.getDefaultSearchEngine() ?: return
        val searchUri = urlFactory(category, query)
        openNewWebTab(searchUri)
    }

}