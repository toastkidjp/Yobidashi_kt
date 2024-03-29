/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.detail.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class ContentViewerFragmentViewModel {

    private val _title = mutableStateOf("")

    val title: State<String> = _title

    fun setTitle(newTitle: String) {
        _title.value = newTitle
    }

    private val _content = mutableStateOf("")

    val content: State<String> = _content

    fun setContent(newContent: String) {
        _content.value = newContent
    }

}