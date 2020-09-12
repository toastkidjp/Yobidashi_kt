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
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.DraggableTouchListener
import jp.toastkid.todo.R
import jp.toastkid.todo.data.TodoTaskDatabase
import jp.toastkid.todo.databinding.AppBarBoardBinding
import jp.toastkid.todo.databinding.FragmentTaskBoardBinding
import jp.toastkid.todo.model.TodoTask
import jp.toastkid.todo.view.TaskListFragmentViewModel
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
class BoardFragment : Fragment() {

    private lateinit var binding: FragmentTaskBoardBinding

    private lateinit var appBarBinding: AppBarBoardBinding

    private val tasks = mutableListOf<TodoTask>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_task_board, container, false)
        appBarBinding = DataBindingUtil.inflate(inflater, R.layout.app_bar_board, container, false)
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

        val refresh = {

        }

        val taskAdditionDialogFragmentUseCase =
                TaskAdditionDialogFragmentUseCase(this, {
                    it.id = tasks.size + 1
                    tasks.add(it)
                    val itemView = layoutInflater.inflate(R.layout.item_task_short, binding.board, false)
                    itemView.findViewById<View>(R.id.color).setBackgroundColor(it.color)
                    itemView.findViewById<TextView>(R.id.main_text).text = it.description
                    itemView.findViewById<ImageView>(R.id.menu).setColorFilter(PreferenceApplier(view.context).color)
                    val draggableTouchListener = DraggableTouchListener()
                    draggableTouchListener.setCallback(object : DraggableTouchListener.OnNewPosition {

                        override fun onNewPosition(x: Float, y: Float) {
                            it.x = x
                            it.y = y
                        }

                    })
                    itemView.setOnTouchListener(draggableTouchListener)

                    binding.board.addView(itemView)
                })

        popup = ItemMenuPopup(
                view.context,
                ItemMenuPopupActionUseCase(
                        repository,
                        { taskAdditionDialogFragmentUseCase.invoke(it) },
                        refresh
                )
        )

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
                        //adapter.submitData(it)
                        it.map { item ->

                            item
                        }
                    }
        }

        appBarBinding.add.setOnClickListener {
            taskAdditionDialogFragmentUseCase.invoke()
        }

        ViewModelProvider(requireActivity()).get(AppBarViewModel::class.java)
                .replace(appBarBinding.root)
    }

}