/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.listener

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import jp.toastkid.article_viewer.R
import jp.toastkid.lib.ContentViewModel

class ArticleLoadStateListener(
    private val contentViewModel: ContentViewModel?,
    private val countSupplier: () -> Int,
    private val stringResolver: (Int) -> String
) : (CombinedLoadStates) -> Unit {

    private var shouldShowNextTime = false

    override fun invoke(p1: CombinedLoadStates) {
        if (!shouldShowNextTime) {
            shouldShowNextTime = p1.refresh is LoadState.Loading
            return
        }

        shouldShowNextTime = false

        val newCount = countSupplier()
        contentViewModel?.snackShort(
            if (newCount != 0)
                String.format(
                    stringResolver(R.string.message_done_article_search),
                    newCount
                )
            else
                stringResolver(R.string.message_not_found_article_search)
        )
    }

}