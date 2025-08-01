/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.detail.viewmodel

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.markdown.domain.model.entity.Markdown
import jp.toastkid.markdown.domain.service.MarkdownParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class ArticleContentViewModel {

    private val _title = mutableStateOf("")

    fun title() = _title.value

    fun setTitle(newTitle: String) {
        _title.value = newTitle
    }

    private val _content = mutableStateOf(Markdown("Now loading..."))

    fun content(): Markdown = _content.value

    private val markdownParser = MarkdownParser()

    fun setContent(newContent: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _content.value = markdownParser.invoke(newContent, _title.value)
        }
    }

    private val scrollState = ScrollState(0)

    fun scrollState() = scrollState

    private val fontColor = AtomicReference(Color.Transparent)

    fun fontColor(): Color = fontColor.get()

    private val backgroundColor = mutableStateOf(Color.Transparent)

    fun backgroundColor(): Color = backgroundColor.value

    fun setPreference(preferenceApplier: PreferenceApplier) {
        fontColor.set(Color(preferenceApplier.editorFontColor()))
        backgroundColor.value = Color(preferenceApplier.editorBackgroundColor())
    }

}