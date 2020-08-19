/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list.sort.comparator

import jp.toastkid.article_viewer.article.list.SearchResult

/**
 * @author toastkidjp
 */
class LengthComparator : Comparator<SearchResult> {

    override fun compare(o1: SearchResult?, o2: SearchResult?): Int {
        return o2?.length?.compareTo(o1?.length ?: 0) ?: 0
    }

}