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
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class TaskListFragment : Fragment() {

    private lateinit var binding: FragmentTaskListBinding

    private lateinit var appBarBinding: AppBarTaskListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        appBarBinding = DataBindingUtil.inflate(inflater, APP_BAR_LAYOUT_ID, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = TodoTaskDatabase.find(view.context).repository()

        var popup: ItemMenuPopup? = null

        val viewModel = ViewModelProvider(this).get(TaskListFragmentViewModel::class.java)
        viewModel
                .showMenu
                .observe(viewLifecycleOwner, Observer { event ->
                    event.getContentIfNotHandled()?.let {
                        popup?.show(it.first, it.second)
                    }
                })

        val adapter = Adapter(viewModel)

        val refresh = { adapter.refresh() }

        val taskAdditionDialogFragmentUseCase =
                TaskAdditionDialogFragmentUseCase(this, {
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.IO) {
                            repository.insert(it)
                        }
                        refresh()
                    }
                })

        popup = ItemMenuPopup(
                view.context,
                ItemMenuPopupActionUseCase(
                        { taskAdditionDialogFragmentUseCase.invoke(it) },
                        {
                            CoroutineScope(Dispatchers.Main).launch {
                                withContext(Dispatchers.IO) {
                                    repository.delete(it)
                                }
                                refresh()
                            }
                        },
                        refresh
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

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_task_list

        @LayoutRes
        private val APP_BAR_LAYOUT_ID = R.layout.app_bar_task_list

    }
}