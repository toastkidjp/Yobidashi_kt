/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.domain.model.entity

import jp.toastkid.markdown.domain.model.data.Line

data class Markdown(
    private val title: String,
    private val lines: MutableList<Line> = mutableListOf()
) {

    fun title() = title

    fun add(line: Line) {
        lines.add(line)
    }

    fun addAll(lines: List<Line>) {
        this.lines.addAll(lines)
    }

    fun lines(): List<Line> = lines

}