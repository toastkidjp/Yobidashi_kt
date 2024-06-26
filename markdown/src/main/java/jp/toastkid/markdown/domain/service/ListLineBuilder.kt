/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.domain.service

import jp.toastkid.markdown.domain.model.data.ListLine

class ListLineBuilder {

    private val list: MutableList<String> = mutableListOf()

    private var ordered: Boolean = false

    private var taskList: Boolean = false

    fun clear() {
        list.clear()
        ordered = false
        taskList = false
    }

    fun add(item: String) {
        list.add(item.substring(item.indexOf(" ") + 1))
    }

    fun setOrdered() {
        ordered = true
    }

    fun setTaskList() {
        taskList = true
    }

    fun isNotEmpty() = list.isNotEmpty()

    fun build() = ListLine(list.toList(), ordered, taskList)

}