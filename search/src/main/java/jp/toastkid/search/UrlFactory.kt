/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.search

import android.net.Uri
import androidx.core.net.toUri

/**
 * @author toastkidjp
 */
class UrlFactory {

    /**
     * Make search [Uri].
     *
     * @param category [SearchCategory]
     * @param query Search query
     * @param currentUrl Use for finding out current search category(Nullable)
     */
    operator fun invoke(
            category: String,
            query: String,
            currentUrl: String? = null
    ): Uri = SearchCategory.findByCategory(category).make(query, currentUrl).toUri()

}
