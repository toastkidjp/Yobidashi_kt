/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.addition

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.todo.R
import jp.toastkid.todo.data.TodoTaskDatabase
import jp.toastkid.todo.databinding.DialogTaskAdditionBinding
import jp.toastkid.todo.model.TodoTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class TaskAdditionDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogTaskAdditionBinding

    private var viewModel: TaskAdditionDialogFragmentViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_task_addition, container, false)
        binding.dialog = this
        viewModel = targetFragment?.let {
            ViewModelProvider(it).get(TaskAdditionDialogFragmentViewModel::class.java)
        }
        return binding.root
    }

    fun add() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val task = TodoTask(0)
                task.description = binding.additionQueryInput.text.toString()
                task.created = System.currentTimeMillis()
                task.lastModified = System.currentTimeMillis()
                task.dueDate = binding.datePicker.minDate
                task.color = (binding.root.findViewById<View>(binding.colors.checkedRadioButtonId)?.background as? ColorDrawable)?.color ?: Color.TRANSPARENT
                TodoTaskDatabase.find(requireContext()).repository().insert(task)
            }
            viewModel?.refresh()
        }
    }

}