/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.board

import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.todo.R
import jp.toastkid.todo.data.TodoTaskDataAccessor
import jp.toastkid.todo.data.TodoTaskDataAccessorFactory
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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBoardUi() {
    val context = LocalContext.current as? ComponentActivity ?: return
    val taskAdditionDialogFragmentViewModel =
        remember { TaskAdditionDialogFragmentViewModel() }

    val repository = remember { TodoTaskDataAccessorFactory().invoke(context) }
    val preferenceApplier = PreferenceApplier(context)

    val menuUseCase = ItemMenuPopupActionUseCase(
        {
            taskAdditionDialogFragmentViewModel?.setTask(it)
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

    val coroutineScope = rememberCoroutineScope()

    val tasks = remember { mutableStateOf<Flow<PagingData<TodoTask>>?>(null) }

    LaunchedEffect(key1 = "init", block = {
        val flow = withContext(Dispatchers.IO) {
            Pager(
                PagingConfig(pageSize = 10, enablePlaceholders = true),
                pagingSourceFactory = { repository.allTasks() }
            )
                .flow
                .cachedIn(coroutineScope)
        }
        tasks.value = flow
    })

    ViewModelProvider(context).get(ContentViewModel::class.java)
        .replaceAppBarContent {
            AppBarUi {
                taskAdditionDialogFragmentViewModel?.setTask(null)
                taskAdditionDialogFragmentViewModel.show()
            }
        }

    TaskEditorUi(
        { TaskBoard(tasks.value, menuUseCase) },
        taskAdditionDialogFragmentViewModel,
        {
            CoroutineScope(Dispatchers.IO).launch {
                repository.insert(it)
            }
        },
        preferenceApplier.colorPair()
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskBoard(flow: Flow<PagingData<TodoTask>>?, menuUseCase: ItemMenuPopupActionUseCase) {
    val context = LocalContext.current
    val repository = remember { TodoTaskDataAccessorFactory().invoke(context) }
    val color = PreferenceApplier(context).color

    val tasks = flow?.collectAsLazyPagingItems() ?: return

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(tasks, { it.id }) { task ->
            task ?: return@items
            BoardItem(
                task,
                repository,
                color,
                menuUseCase,
                Modifier.animateItemPlacement()
            )
        }
    }
}

@Composable
private fun BoardItem(
    task: TodoTask,
    repository: TodoTaskDataAccessor,
    color: Int,
    menuUseCase: ItemMenuPopupActionUseCase,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(
        stringResource(id = R.string.modify),
        stringResource(id = R.string.delete)
    )

    var offsetX by remember { mutableStateOf(task.x) }
    var offsetY by remember { mutableStateOf(task.y) }

    Surface(
        shadowElevation = 4.dp,
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp)
            .width(140.dp)
            .height(140.dp)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        task.x = offsetX
                        task.y = offsetY
                        CoroutineScope(Dispatchers.IO).launch {
                            repository.insert(task)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
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
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
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