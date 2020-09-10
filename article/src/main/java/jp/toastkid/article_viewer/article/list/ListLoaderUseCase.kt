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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ListLoaderUseCase(private val adapter: Adapter) {

    private var lastJob: Job? = null

    operator fun invoke(pagingSourceFactory: () -> PagingSource<Int, SearchResult>) {
        lastJob?.cancel()
        lastJob = CoroutineScope(Dispatchers.IO).launch {
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
        lastJob?.cancel()
    }
}