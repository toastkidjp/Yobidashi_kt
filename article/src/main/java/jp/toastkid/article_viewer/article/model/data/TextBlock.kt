/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.model.data

data class TextBlock(
    val text: String,
    val level: Int = -1,
    val quote: Boolean = false
) : Line {

    fun fontSize() = when (level) {
        1 -> 32
        2 -> 26
        3 -> 22
        4 -> 20
        5 -> 18
        6 -> 14
        else -> 16
    }

}