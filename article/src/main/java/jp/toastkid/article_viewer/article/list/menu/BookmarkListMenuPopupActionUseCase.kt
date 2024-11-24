/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list.menu

import android.content.Context
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository
import jp.toastkid.lib.clip.Clipboard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class BookmarkListMenuPopupActionUseCase(
    private val articleRepository: ArticleRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val deleted: () -> Unit,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MenuPopupActionUseCase {

    override fun copySource(context: Context, id: Int) {
        CoroutineScope(ioDispatcher).launch {
            val article = withContext(ioDispatcher) {
                val article = articleRepository.findContentById(id)
                return@withContext article
            } ?: return@launch
            Clipboard.clip(context, article)
        }
    }

    override fun addToBookmark(id: Int) = Unit

    override fun delete(id: Int) {
        CoroutineScope(mainDispatcher).launch {
            withContext(ioDispatcher) {
                bookmarkRepository.delete(id)
            }
            deleted()
        }
    }

}