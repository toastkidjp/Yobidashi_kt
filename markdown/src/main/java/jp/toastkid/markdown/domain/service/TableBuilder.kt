/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.domain.service


import jp.toastkid.markdown.domain.model.data.TableLine
import java.util.concurrent.atomic.AtomicBoolean

class TableBuilder {

    private val table = mutableListOf<List<Any>>()

    private val active = AtomicBoolean(false)

    private var columnNames: List<Any>? = null

    fun hasColumns(): Boolean = columnNames != null

    fun setColumns(line: String) {
        table.clear()
        columnNames = line.split("|").filter { it.isNotEmpty() }
    }

    fun addTableLines(line: String) {
        line.split("|").drop(1).also {
            table.add(it)
        }
    }

    fun build() = TableLine(columnNames ?: emptyList(), table.toList())

    fun active() = active.get()

    fun setActive() {
        active.set(true)
    }

    fun setInactive() {
        active.set(false)
    }

    fun clear() {
        table.clear()
        columnNames = null
    }

    companion object {

        fun isTableStart(line: String) = line.startsWith("|")

        fun shouldIgnoreLine(line: String) = line.startsWith("|:---")

    }

}