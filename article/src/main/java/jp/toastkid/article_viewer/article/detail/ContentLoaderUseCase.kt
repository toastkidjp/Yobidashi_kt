/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import android.widget.TextView
import io.noties.markwon.Markwon
import jp.toastkid.article_viewer.article.ArticleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class ContentLoaderUseCase(
        private val repository: ArticleRepository,
        private val markwon: Markwon,
        private val contentView: TextView,
        private val subheads: MutableList<String>
) {

    operator fun invoke(title: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val content = withContext(Dispatchers.IO) { repository.findContentByTitle(title) }
                    ?: return@launch

            markwon.setMarkdown(contentView, content)

            LinkGeneratorService().invoke(contentView)

            withContext(Dispatchers.Default) {
                content.split(System.lineSeparator())
                        .filter { it.startsWith("#") }
                        .forEach { subheads.add(it) }
            }
        }
    }
}