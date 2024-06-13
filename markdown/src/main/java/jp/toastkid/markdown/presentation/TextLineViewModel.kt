/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import jp.toastkid.markdown.domain.service.LinkBehaviorService
import jp.toastkid.markdown.domain.service.LinkGenerator
import jp.toastkid.ui.text.KeywordHighlighter

class TextLineViewModel(private val linkBehaviorService: LinkBehaviorService) {

    private val lastLayoutResult = mutableStateOf<TextLayoutResult?>(null)

    private val linkGenerator = LinkGenerator()

    private val annotatedString = mutableStateOf(AnnotatedString(""))

    private val keywordHighlighter = KeywordHighlighter()

    private fun annotate(text: String, finderTarget: String? = null) = keywordHighlighter(text, finderTarget)

    fun annotatedString() = annotatedString.value

    suspend fun launch(text: String) {
        annotatedString.value = annotate(linkGenerator.invoke(text))
    }

    fun putLayoutResult(layoutResult: TextLayoutResult) {
        lastLayoutResult.value = layoutResult
    }

    fun onClick(it: Int) {
        //val textLayoutResult = lastLayoutResult.value ?: return

        val stringRange = annotatedString
            .value
            .getStringAnnotations(tag = "URL", start = it, end = it)
            .firstOrNull() ?: return

        linkBehaviorService.invoke(stringRange.item)
    }

}