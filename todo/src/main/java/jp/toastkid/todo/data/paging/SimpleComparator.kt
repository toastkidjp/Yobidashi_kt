/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.data.paging

import androidx.recyclerview.widget.DiffUtil
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class SimpleComparator : DiffUtil.ItemCallback<TodoTask>() {

    override fun areItemsTheSame(oldItem: TodoTask, newItem: TodoTask): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TodoTask, newItem: TodoTask): Boolean {
        return oldItem.equals(newItem)
    }
}