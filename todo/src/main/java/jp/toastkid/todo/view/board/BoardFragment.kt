/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.board

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.todo.R
import jp.toastkid.todo.databinding.AppBarBoardBinding
import jp.toastkid.todo.databinding.FragmentTaskBoardBinding
import jp.toastkid.todo.model.TodoTask
import jp.toastkid.todo.view.addition.TaskAdditionDialogFragmentUseCase
import jp.toastkid.todo.view.item.menu.ItemMenuPopup
import jp.toastkid.todo.view.item.menu.ItemMenuPopupActionUseCase
import jp.toastkid.todo.view.list.TaskListFragmentViewModel

/**
 * @author toastkidjp
 */
class BoardFragment : Fragment() {

    private lateinit var binding: FragmentTaskBoardBinding

    private lateinit var appBarBinding: AppBarBoardBinding

    private var taskAdditionDialogFragmentUseCase: TaskAdditionDialogFragmentUseCase? = null

    private val tasks = mutableListOf<TodoTask>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_task_board, container, false)
        appBarBinding = DataBindingUtil.inflate(inflater, R.layout.app_bar_board, container, false)
        appBarBinding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var popup: ItemMenuPopup? = null
        var taskAddingUseCase: TaskAddingUseCase? = null

        val viewModel = ViewModelProvider(this).get(TaskListFragmentViewModel::class.java)
        viewModel
                .showMenu
                .observe(viewLifecycleOwner, Observer { event ->
                    event.getContentIfNotHandled()?.let {
                        popup?.show(it.first, it.second)
                    }
                })

        taskAdditionDialogFragmentUseCase =
                TaskAdditionDialogFragmentUseCase(this) {
                    val firstOrNull = tasks.firstOrNull { task -> task.lastModified == it.lastModified }
                    if (firstOrNull == null) {
                        it.id = tasks.size + 1
                        taskAddingUseCase?.invoke(it)
                        return@TaskAdditionDialogFragmentUseCase
                    }

                    tasks.remove(firstOrNull)
                    removeTask(firstOrNull)
                    taskAddingUseCase?.invoke(firstOrNull)
                }

        popup = ItemMenuPopup(
                view.context,
                ItemMenuPopupActionUseCase(
                        { taskAdditionDialogFragmentUseCase?.invoke(it) },
                        ::removeTask
                )
        )

        taskAddingUseCase = TaskAddingUseCase(
                PreferenceApplier(requireContext()).color,
                tasks,
                binding.board,
                BoardItemViewFactory(layoutInflater) { parent, showTask ->
                    popup?.show(parent, showTask)
                }
        )

        appBarBinding.clear.setOnClickListener {
            clearTasks(taskAddingUseCase)
        }

        ViewModelProvider(requireActivity()).get(AppBarViewModel::class.java)
                .replace(appBarBinding.root)

        taskAddingUseCase.invoke(makeSampleTask())
    }

    private fun makeSampleTask() = SampleTaskMaker().invoke()

    private fun removeTask(task: TodoTask) {
        tasks.remove(task)
        binding.board.children
                .firstOrNull { it.tag == task.id }
                ?.also { binding.board.removeView(it) }
    }

    private fun clearTasks(taskAddingUseCase: TaskAddingUseCase) {
        val keep = mutableListOf<TodoTask>().also {
            it.addAll(tasks)
        }
        tasks.clear()
        binding.board.removeAllViews()
        ViewModelProvider(requireActivity()).get(ContentViewModel::class.java)
                .snackWithAction("Clear all tasks.", "Undo", {
                    keep.forEach { taskAddingUseCase.invoke(it) }
                })
    }

    fun addTask() {
        taskAdditionDialogFragmentUseCase?.invoke()
    }

}