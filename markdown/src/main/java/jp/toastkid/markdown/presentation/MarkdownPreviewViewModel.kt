/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.markdown.presentation

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.ui.text.font.FontWeight

class MarkdownPreviewViewModel(scrollState: ScrollableState) {

    fun extractText(it: String, taskList: Boolean): String {
        return if (taskList) it.substring(it.indexOf("] ") + 1) else it
    }

    fun makeFontWeight(level: Int): FontWeight {
        return if (level != -1) FontWeight.Bold else FontWeight.Normal
    }

    fun makeTopMargin(level: Int): Int {
        return when (level) {
            1, 2 -> 12
            3 -> 8
            4 -> 4
            else -> 0
        }
    }

}