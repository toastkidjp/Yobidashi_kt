/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article

import androidx.room.Entity
import androidx.room.PrimaryKey
import jp.toastkid.article_viewer.article.list.SearchResult

/**
 * @author toastkidjp
 */
@Entity
class Article(
    @PrimaryKey
    var id: Int
) {

    var title: String = ""

    var contentText: String = ""

    var lastModified: Long = 0L

    var length: Int = 0

    var bigram: String = ""

}