/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list.sort

import androidx.paging.PagingSource
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.list.SearchResult

/**
 * @author toastkidjp
 */
enum class Sort(private val sort: (ArticleRepository) -> PagingSource<Int, SearchResult>) {

    LAST_MODIFIED({ it.orderByLastModified() }),
    NAME({ it.orderByName() }),
    LENGTH({ it.orderByLength() });

    operator fun invoke(repository: ArticleRepository) = sort(repository)

    companion object {

        fun titles(): Array<String> {
            return entries.map { it.name }.toTypedArray()
        }

        fun findCurrentIndex(name: String): Int {
            entries.forEachIndexed { index, userAgent ->
                if (userAgent.name == name) {
                    return index
                }
            }
            return 0
        }

        fun findByName(name: String?): Sort {
            return entries.firstOrNull { it.name.equals(name, true) } ?: LAST_MODIFIED
        }

    }
}