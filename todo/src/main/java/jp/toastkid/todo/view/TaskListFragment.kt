/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.Pager
import androidx.paging.PagingConfig
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.todo.R
import jp.toastkid.todo.data.TodoTaskDatabase
import jp.toastkid.todo.databinding.AppBarTaskListBinding
import jp.toastkid.todo.databinding.FragmentTaskListBinding
import jp.toastkid.todo.view.addition.TaskAdditionDialogFragmentUseCase
import jp.toastkid.todo.view.initial.InitialTaskPreparation
import jp.toastkid.todo.view.item.menu.ItemMenuPopup
import jp.toastkid.todo.view.item.menu.ItemMenuPopupActionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class TaskListFragment : Fragment() {

    private lateinit var binding: FragmentTaskListBinding

    private lateinit var appBarBinding: AppBarTaskListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_task_list, container, false)

        appBarBinding = DataBindingUtil.inflate(inflater, R.layout.app_bar_task_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = TodoTaskDatabase.find(view.context).repository()

        var popup: ItemMenuPopup? = null

        val adapter = Adapter { parent, item -> popup?.show(parent, item) }

        val taskAdditionDialogFragmentUseCase =
                TaskAdditionDialogFragmentUseCase(this, { adapter.refresh() })

        popup = ItemMenuPopup(
                view.context,
                ItemMenuPopupActionUseCase(
                        TodoTaskDatabase.find(view.context).repository(),
                        { taskAdditionDialogFragmentUseCase.invoke(it) },
                        { adapter.refresh() }
                )
        )

        binding.results.adapter = adapter
        CoroutineScope(Dispatchers.IO).launch {
            if (repository.count() == 0) {
                InitialTaskPreparation(repository).invoke()
            }
            Pager(
                    PagingConfig(pageSize = 10, enablePlaceholders = true),
                    pagingSourceFactory = { repository.allTasks() }
            )
                    .flow
                    .collectLatest {
                        adapter.submitData(it)
                    }
        }

        appBarBinding.add.setOnClickListener {
            taskAdditionDialogFragmentUseCase.invoke()
        }

        ViewModelProvider(requireActivity()).get(AppBarViewModel::class.java)
                .replace(appBarBinding.root)
    }

}