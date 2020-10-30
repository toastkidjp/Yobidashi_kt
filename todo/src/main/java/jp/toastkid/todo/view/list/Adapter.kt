/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import jp.toastkid.todo.R
import jp.toastkid.todo.data.paging.SimpleComparator
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class Adapter(private val viewModel: TaskListFragmentViewModel) : PagingDataAdapter<TodoTask, ViewHolder>(SimpleComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_task, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position) ?: return
        holder.bind(result)
        holder.setOnMenuClick { viewModel.showMenu(it, result) }
    }

}