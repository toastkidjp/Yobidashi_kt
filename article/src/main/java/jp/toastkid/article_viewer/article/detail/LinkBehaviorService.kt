/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import androidx.core.net.toUri
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel

/**
 * @author toastkidjp
 */
class LinkBehaviorService(
        private val contentViewModel: ContentViewModel,
        private val browserViewModel: BrowserViewModel,
        private val exists: (String) -> Boolean
) {

    operator fun invoke(url: String?) {
        if (url.isNullOrBlank()) {
            return
        }

        if (!InternalLinkScheme.isInternalLink(url)) {
            browserViewModel.open(url.toUri())
            return
        }

        val title = InternalLinkScheme.extract(url)
        if (exists(title)) {
            contentViewModel.newArticle(title)
        }
    }
}