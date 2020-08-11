/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list.sort

import jp.toastkid.article_viewer.article.list.SearchResult
import jp.toastkid.article_viewer.article.list.sort.comparator.LastModifiedComparator
import jp.toastkid.article_viewer.article.list.sort.comparator.LengthComparator
import jp.toastkid.article_viewer.article.list.sort.comparator.NameComparator

/**
 * @author toastkidjp
 */
enum class Sort(val comparator: Comparator<SearchResult>) {

    LAST_MODIFIED(LastModifiedComparator()),
    NAME(NameComparator()),
    LENGTH(LengthComparator());

    companion object {

        fun titles(): Array<String> {
            return values().map { it.name }.toTypedArray()
        }

        fun findCurrentIndex(name: String): Int {
            values().forEachIndexed { index, userAgent ->
                if (userAgent.name == name) {
                    return index
                }
            }
            return 0
        }

        fun findByName(name: String?): Sort {
            return values().firstOrNull { it.name.equals(name, true) } ?: LAST_MODIFIED
        }

    }
}