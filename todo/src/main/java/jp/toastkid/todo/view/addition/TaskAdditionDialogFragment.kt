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
import android.widget.RadioButton
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.todo.R
import jp.toastkid.todo.databinding.DialogTaskAdditionBinding
import jp.toastkid.todo.model.TodoTask
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * @author toastkidjp
 */
class TaskAdditionDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogTaskAdditionBinding

    private var viewModel: TaskAdditionDialogFragmentViewModel? = null

    private var task: TodoTask? = null

    private var date: Triple<Int, Int, Int>? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.dialog = this

        task = arguments?.getSerializable(KEY_EXTRA) as? TodoTask
        val today = Calendar.getInstance()
        task?.let {
            today.timeInMillis = it.dueDate
        }
        binding.datePicker.init(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            date = Triple(year, monthOfYear, dayOfMonth)
        }

        viewModel = arguments?.getSerializable(KEY_VIEW_MODEL) as? TaskAdditionDialogFragmentViewModel

        task?.let {
            binding.additionQueryInput.setText(it.description)
            binding.additionQueryInput.setSelection(it.description.length)
            (binding.colors.children
                    .firstOrNull { checkbox -> extractBackgroundColor(checkbox) == it.color }
                    as? RadioButton)?.isChecked = true
        }
        return binding.root
    }

    fun add() {
        val task = this.task ?: TodoTask(0)
        updateTask(task)

        viewModel?.refresh(task)

        if (this.task != null) {
            dismiss()
        }
    }

    private fun updateTask(task: TodoTask) {
        task.description = binding.additionQueryInput.text.toString()
        if (this@TaskAdditionDialogFragment.task == null) {
            task.created = System.currentTimeMillis()
        }
        task.lastModified = System.currentTimeMillis()
        task.dueDate = makeDateMs()

        val checkedRadioButtonId = binding.colors.checkedRadioButtonId
        if (checkedRadioButtonId != -1) {
            task.color = extractBackgroundColor(binding.root.findViewById(checkedRadioButtonId))
        }
    }

    private fun makeDateMs() =
            if (date == null)
                System.currentTimeMillis()
            else
                GregorianCalendar(
                        date?.first ?: 0,
                        date?.second ?: 0,
                        date?.third ?: 0
                ).timeInMillis

    private fun extractBackgroundColor(view: View?) =
            (view?.background as? ColorDrawable)?.color
                    ?: Color.TRANSPARENT

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.dialog_task_addition

        private const val KEY_EXTRA = "task"

        private const val KEY_VIEW_MODEL = "view_model"

        fun make(
            task: TodoTask? = null,
            taskAdditionDialogFragmentViewModel: TaskAdditionDialogFragmentViewModel
        ): DialogFragment = TaskAdditionDialogFragment().also {
            it.arguments = bundleOf(KEY_VIEW_MODEL to taskAdditionDialogFragmentViewModel)
            if (task != null) {
                it.arguments?.putSerializable(KEY_EXTRA, task)
            }
        }
    }

}