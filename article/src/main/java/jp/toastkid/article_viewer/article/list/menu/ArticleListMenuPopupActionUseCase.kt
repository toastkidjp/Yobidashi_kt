/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list.menu

import androidx.annotation.VisibleForTesting
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.bookmark.Bookmark
import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class ArticleListMenuPopupActionUseCase(
    private val articleRepository: ArticleRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val deleted: (Article) -> Unit,
    @VisibleForTesting private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    @VisibleForTesting private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MenuPopupActionUseCase {

    override fun addToBookmark(id: Int) {
        CoroutineScope(ioDispatcher).launch {
            bookmarkRepository.add(Bookmark(id))
        }
    }

    override fun delete(id: Int) {
        CoroutineScope(mainDispatcher).launch {
            val article = withContext(ioDispatcher) {
                val article = articleRepository.findArticleById(id)
                articleRepository.delete(id)
                return@withContext article
            }
            deleted(article)
        }
    }
}