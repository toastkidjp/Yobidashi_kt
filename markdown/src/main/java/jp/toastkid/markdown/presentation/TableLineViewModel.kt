/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class TableLineViewModel {

    private var lastSorted = -1 to false

    private val tableData: MutableState<List<List<Any>>> =  mutableStateOf(emptyList())

    private val headerCursorOn = mutableStateOf(false)

    private val tableSortService = TableSortService()

    fun tableData() = tableData.value

    fun start(table: List<List<Any>>) {
        tableData.value = table
    }

    private fun sort(
        lastSortOrder: Boolean,
        index: Int,
        articleStates: MutableState<List<List<Any>>>
    ) {
        val newItems = tableSortService.invoke(lastSortOrder, index, articleStates.value)
        if (newItems != null) {
            articleStates.value = newItems
        }
    }

    fun clickHeaderColumn(index: Int) {
        val lastSortOrder = if (lastSorted.first == index) lastSorted.second else false
        lastSorted = index to lastSortOrder.not()

        sort(lastSortOrder, index, tableData)
    }

    fun setCursorOnHeader() {
        headerCursorOn.value = true
    }

    fun setCursorOffHeader() {
        headerCursorOn.value = false
    }

    fun onCursorOnHeader() = headerCursorOn.value

}