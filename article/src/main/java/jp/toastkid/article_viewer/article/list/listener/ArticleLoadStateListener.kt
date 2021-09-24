/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.listener

import androidx.paging.CombinedLoadStates
import jp.toastkid.article_viewer.R
import jp.toastkid.lib.ContentViewModel

class ArticleLoadStateListener(
    private val contentViewModel: ContentViewModel?,
    private val countSupplier: () -> Int,
    private val stringResolver: (Int) -> String
) : (CombinedLoadStates) -> Unit {

    override fun invoke(p1: CombinedLoadStates) {
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