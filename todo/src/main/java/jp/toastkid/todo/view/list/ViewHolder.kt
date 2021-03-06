/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.list

import android.text.format.DateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.todo.databinding.ItemTaskBinding
import jp.toastkid.todo.model.TodoTask

/**
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(task: TodoTask) {
        binding.color.setBackgroundColor(task.color)
        binding.mainText.text = task.description
        binding.subText.text = DateFormat.format("yyyy/MM/dd(E)", task.dueDate)
        binding.menu.setColorFilter(PreferenceApplier(binding.menu.context).color)
    }

    fun setOnMenuClick(onAction: (View) -> Unit) {
        binding.menu.setOnClickListener { onAction(it) }
    }

}