/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import androidx.core.net.toUri
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class LinkBehaviorService(
        private val repository: ArticleRepository,
        private val contentViewModel: ContentViewModel,
        private val browserViewModel: BrowserViewModel
) {

    operator fun invoke(url: String?) {
        if (url.isNullOrBlank()) {
            return
        }

        if (!InternalLinkScheme.isInternalLink(url)) {
            browserViewModel.open(url.toUri())
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val title = InternalLinkScheme.extract(url)
            val content = withContext(Dispatchers.IO) {
                repository.findContentByTitle(title)
            }

            if (content.isNullOrBlank()) {
                return@launch
            }

            contentViewModel.newArticle(title, content)
        }
    }
}