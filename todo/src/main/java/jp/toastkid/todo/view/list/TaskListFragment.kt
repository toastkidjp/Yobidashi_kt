/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.todo.view.list

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.todo.R
import jp.toastkid.todo.data.TodoTaskDataAccessor
import jp.toastkid.todo.data.TodoTaskDatabase
import jp.toastkid.todo.model.TodoTask
import jp.toastkid.todo.view.addition.TaskAdditionDialogFragmentViewModel
import jp.toastkid.todo.view.addition.TaskEditorUi
import jp.toastkid.todo.view.appbar.AppBarUi
import jp.toastkid.todo.view.item.menu.ItemMenuPopupActionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class TaskListFragment : Fragment(), ContentScrollable {

    private var scrollState: LazyListState? = null

    private var repository: TodoTaskDataAccessor? = null

    private var taskAdditionDialogFragmentViewModel: TaskAdditionDialogFragmentViewModel? = null

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = activity ?: return null
        val composeView = ComposeView(context)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )

        val repository = TodoTaskDatabase.find(context).repository()
        this.repository = repository

        taskAdditionDialogFragmentViewModel =
            ViewModelProvider(this).get(TaskAdditionDialogFragmentViewModel::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            val flow = withContext(Dispatchers.IO) {
                Pager(
                    PagingConfig(pageSize = 10, enablePlaceholders = true),
                    pagingSourceFactory = { repository.allTasks() }
                )
                    .flow
                    .cachedIn(lifecycleScope)
            }
            composeView.setContent {
                val preferenceApplier = PreferenceApplier(context)
                val bottomSheetScaffoldState = rememberModalBottomSheetState(
                    ModalBottomSheetValue.Hidden
                )

                TaskEditorUi(
                    { TaskListUi(flow) },
                    taskAdditionDialogFragmentViewModel,
                    bottomSheetScaffoldState,
                    {
                        CoroutineScope(Dispatchers.IO).launch {
                            repository.insert(it)
                        }
                    },
                    colorPair = preferenceApplier.colorPair()
                )

                val coroutineScope = rememberCoroutineScope()

                taskAdditionDialogFragmentViewModel?.task?.observe(viewLifecycleOwner, {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.show()
                    }
                })
            }
        }

        ViewModelProvider(context).get(AppBarViewModel::class.java)
            .replace(context) {
                AppBarUi {
                    taskAdditionDialogFragmentViewModel?.setTask(null)
                }
            }

        return composeView
    }

    @Composable
    fun TaskListUi(flow: Flow<PagingData<TodoTask>>) {
        val context = view?.context ?: return
        val color = PreferenceApplier(context).color

        val listState = rememberLazyListState()
        this.scrollState = listState

        val menuUseCase = ItemMenuPopupActionUseCase(
            { taskAdditionDialogFragmentViewModel?.setTask(it) },
            {
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        repository?.delete(it)
                    }
                }
            }
        )

        val tasks = flow.collectAsLazyPagingItems()

        MaterialTheme {
            LazyColumn(
                state = listState,
                modifier = Modifier.nestedScroll(rememberViewInteropNestedScrollConnection())
            ) {
                items(tasks) { task ->
                    task ?: return@items
                    TaskListItem(task, color, menuUseCase)
                }
            }
        }
    }

    @Composable
    private fun TaskListItem(
        task: TodoTask,
        color: Int,
        menuUseCase: ItemMenuPopupActionUseCase
    ) {
        var expanded by remember { mutableStateOf(false) }
        val items = listOf(
            stringResource(id = R.string.modify),
            stringResource(id = R.string.delete)
        )

        Surface(
            elevation = 4.dp,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Checkbox(
                    checked = task.done,
                    onCheckedChange = {
                        task.done = task.done.not()
                        CoroutineScope(Dispatchers.IO).launch {
                            repository?.insert(task)
                        }
                    },
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight()
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.description,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = DateFormat.format("yyyy/MM/dd(E)", task.dueDate).toString(),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box {
                    AsyncImage(
                        R.drawable.ic_more,
                        contentDescription = stringResource(R.string.menu),
                        colorFilter = ColorFilter.tint(
                            Color(color),
                            BlendMode.SrcIn
                        ),
                        modifier = Modifier
                            .width(32.dp)
                            .fillMaxHeight()
                            .clickable {
                                expanded = true
                            }
                            .background(color = Color(task.color))
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(colorResource(id = R.color.soft_background))
                    ) {
                        items.forEachIndexed { index, s ->
                            DropdownMenuItem(onClick = {
                                when (index) {
                                    0 -> menuUseCase.modify(task)
                                    1 -> menuUseCase.delete(task)
                                }
                                expanded = false
                            }) { Text(text = s) }
                        }
                    }
                }
            }
        }
    }

    override fun toTop() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(0, 0)
        }
    }

    override fun toBottom() {
        CoroutineScope(Dispatchers.Main).launch {
            scrollState?.scrollToItem(scrollState?.layoutInfo?.totalItemsCount ?: 0, 0)
        }
    }

}