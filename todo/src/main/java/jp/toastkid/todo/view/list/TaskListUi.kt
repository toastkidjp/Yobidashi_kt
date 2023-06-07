/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.list

import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.todo.R
import jp.toastkid.todo.data.TodoTaskDataAccessorFactory
import jp.toastkid.todo.model.TodoTask
import jp.toastkid.todo.view.addition.TaskAdditionDialogFragmentViewModel
import jp.toastkid.todo.view.addition.TaskEditorUi
import jp.toastkid.todo.view.appbar.AppBarUi
import jp.toastkid.todo.view.item.menu.ItemMenuPopupActionUseCase
import jp.toastkid.todo.view.list.initial.InitialTaskPreparation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TaskListUi() {
    val context = LocalContext.current as? ComponentActivity ?: return

    val taskAdditionDialogFragmentViewModel =
        remember { TaskAdditionDialogFragmentViewModel() }

    viewModel(ContentViewModel::class.java, context)
        .replaceAppBarContent {
            AppBarUi {
                taskAdditionDialogFragmentViewModel?.setTask(null)
                taskAdditionDialogFragmentViewModel.show()
            }
        }

    val repository = remember { TodoTaskDataAccessorFactory().invoke(context) }

    val tasks = remember { mutableStateOf<Flow<PagingData<TodoTask>>?>(null) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = "load", block = {
        withContext(Dispatchers.IO) {
            if (repository.count() == 0) {
                InitialTaskPreparation(repository).invoke()
            }

            val flow = Pager(
                PagingConfig(pageSize = 10, enablePlaceholders = true),
                pagingSourceFactory = { repository.allTasks() }
            )
                .flow
                .cachedIn(coroutineScope)
            tasks.value = flow
        }
    })

    val menuUseCase = ItemMenuPopupActionUseCase(
        {
            taskAdditionDialogFragmentViewModel.setTask(it)
            taskAdditionDialogFragmentViewModel.show()
        },
        {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    repository.delete(it)
                }
            }
        }
    )

    TaskEditorUi(
        { TaskList(tasks.value, menuUseCase) },
        taskAdditionDialogFragmentViewModel
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insert(it)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskList(
    flow: Flow<PagingData<TodoTask>>?,
    menuUseCase: ItemMenuPopupActionUseCase
) {
    val tasks = flow?.collectAsLazyPagingItems() ?: return

    LazyColumn(state = rememberLazyListState()) {
        items(tasks, { it.id }) { task ->
            task ?: return@items
            TaskListItem(task, menuUseCase, Modifier.animateItemPlacement())
        }
    }
}

@Composable
private fun TaskListItem(
    task: TodoTask,
    menuUseCase: ItemMenuPopupActionUseCase,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(
        stringResource(id = R.string.modify),
        stringResource(id = R.string.delete)
    )

    val repository = TodoTaskDataAccessorFactory().invoke(LocalContext.current)

    Surface(
        shadowElevation = 4.dp,
        modifier = modifier
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
                        repository.insert(task)
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
                Icon(
                    painterResource(id = R.drawable.ic_more),
                    contentDescription = stringResource(R.string.menu),
                    tint = MaterialTheme.colorScheme.secondary,
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
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEachIndexed { index, s ->
                        DropdownMenuItem(
                            text = {
                                Text(text = s)
                            },
                            onClick = {
                            when (index) {
                                0 -> menuUseCase.modify(task)
                                1 -> menuUseCase.delete(task)
                            }
                            expanded = false
                        })
                    }
                }
            }
        }
    }
}
