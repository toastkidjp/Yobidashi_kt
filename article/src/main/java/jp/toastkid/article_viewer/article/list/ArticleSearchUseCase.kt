/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ArticleSearchUseCase(
        private val adapter: Adapter,
        private val repository: ArticleRepository,
        private val preferencesWrapper: PreferenceApplier
) {

    private val tokenizer = NgramTokenizer()

    /**
     * [CompositeDisposable].
     */
    private val disposables = Job()

    fun all() {
        load { repository.getAll() }
    }

    fun search(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            return
        }

        load { repository.search("${tokenizer(keyword, 2)}") }
    }

    fun filter(keyword: String?) {
        if (!preferencesWrapper.useTitleFilter()) {
            return
        }

        if (keyword.isNullOrBlank()) {
            all()
            return
        }

        load { repository.filter(keyword) }
    }

    private fun load(pagingSourceFactory: () -> PagingSource<Int, SearchResult>) {
        CoroutineScope(Dispatchers.IO).launch {
            Pager(
                    PagingConfig(pageSize = 50, enablePlaceholders = true),
                    pagingSourceFactory = pagingSourceFactory
            )
                    .flow
                    .collectLatest {
                        adapter.submitData(it)
                    }
        }
    }

    fun dispose() {
        disposables.cancel()
    }

}