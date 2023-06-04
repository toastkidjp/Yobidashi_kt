/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

/**
 * @author toastkidjp
 */
data class SearchResult(
    val id: Int,
    val title: String,
    val lastModified: Long,
    val length: Int
)