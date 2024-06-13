/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.markdown.domain.service

import androidx.core.net.toUri
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.markdown.domain.model.InternalLinkScheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class LinkBehaviorService(
    private val contentViewModel: ContentViewModel,
    private val exists: (String) -> Boolean,
    private val internalLinkScheme: InternalLinkScheme = InternalLinkScheme(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke(url: String?) {
        if (url.isNullOrBlank()) {
            return
        }

        if (!internalLinkScheme.isInternalLink(url)) {
            contentViewModel.open(url.toUri())
            return
        }

        val title = internalLinkScheme.extract(url)
        CoroutineScope(ioDispatcher).launch {
            val exists = exists(title)
            if (exists) {
                contentViewModel.newArticle(title)
            } else {
                contentViewModel.snackShort("\"$title\" does not exist.")
            }
        }
    }
}