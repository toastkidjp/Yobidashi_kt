/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.domain.service

import jp.toastkid.markdown.domain.model.data.CodeBlockLine

class CodeBlockBuilder {

    private var isInCodeBlock = false

    private val code = StringBuilder()

    private var codeFormat = ""

    fun append(line: String) {
        code.append(if (code.isNotEmpty()) LINE_SEPARATOR else "").append(line)
    }

    fun shouldAppend(line: String): Boolean {
        return isInCodeBlock && !line.startsWith("```")
    }

    fun build(): CodeBlockLine {
        return CodeBlockLine(code.toString(), codeFormat)
    }

    fun inCodeBlock() = isInCodeBlock

    fun initialize() {
        code.setLength(0)
        isInCodeBlock = false
    }

    fun startCodeBlock() {
        this.isInCodeBlock = true
    }

    fun setCodeFormat(codeFormat: String) {
        this.codeFormat = codeFormat
    }

    companion object {

        private val LINE_SEPARATOR = System.lineSeparator()

    }

}