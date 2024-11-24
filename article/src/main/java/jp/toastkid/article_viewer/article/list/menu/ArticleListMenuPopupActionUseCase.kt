/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list.menu

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.ArticleRepositoryFactory
import jp.toastkid.article_viewer.article.data.BookmarkRepositoryFactory
import jp.toastkid.article_viewer.bookmark.Bookmark
import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
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
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MenuPopupActionUseCase {

    override fun copySource(context: Context, id: Int) {
        CoroutineScope(ioDispatcher).launch {
            val article = withContext(ioDispatcher) {
                val article = articleRepository.findArticleById(id)
                return@withContext article
            }
            Clipboard.clip(context, article.contentText)
        }
    }

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

    companion object {

        fun withContext(context: Context): ArticleListMenuPopupActionUseCase {
            val contentViewModel = (context as? ViewModelStoreOwner)?.let {
                ViewModelProvider(it).get(ContentViewModel::class)
            }
            return ArticleListMenuPopupActionUseCase(
                ArticleRepositoryFactory().invoke(context),
                BookmarkRepositoryFactory().invoke(context),
                {
                    contentViewModel?.snackWithAction(
                        "Deleted: \"${it.title}\".",
                        "UNDO"
                    ) { CoroutineScope(Dispatchers.IO).launch { ArticleRepositoryFactory().invoke(context).insert(it) } }
                }
            )
        }

    }

}