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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.todo.R
import jp.toastkid.todo.data.TodoTaskDataAccessorFactory
import jp.toastkid.todo.model.TodoTask
import jp.toastkid.todo.view.addition.TaskEditorUi
import jp.toastkid.todo.view.addition.TaskEditorViewModel
import jp.toastkid.todo.view.appbar.AppBarUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun TaskBoardUi() {
    val context = LocalContext.current as? ComponentActivity ?: return
    val taskEditorViewModel =
        remember { TaskEditorViewModel() }

    val repository = remember { TodoTaskDataAccessorFactory().invoke(context) }

    val coroutineScope = rememberCoroutineScope()

    val tasks = remember { mutableStateOf<Flow<PagingData<TodoTask>>?>(null) }

    LaunchedEffect(key1 = "init", block = {
        ViewModelProvider(context).get(ContentViewModel::class.java)
            .replaceAppBarContent {
                AppBarUi {
                    taskEditorViewModel.setTask(null)
                    taskEditorViewModel.show()
                }
            }

        val flow = withContext(Dispatchers.IO) {
            Pager(
                PagingConfig(pageSize = 10, enablePlaceholders = true),
                pagingSourceFactory = repository::allTasks
            )
                .flow
                .cachedIn(coroutineScope)
        }
        tasks.value = flow
    })

    TaskEditorUi(
        { TaskBoard(tasks.value) {
            taskEditorViewModel.setTask(it)
            taskEditorViewModel.show()
        }
        },
        taskEditorViewModel
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insert(it)
        }
    }
}

@Composable
fun TaskBoard(flow: Flow<PagingData<TodoTask>>?, modify: (TodoTask) -> Unit) {
    val context = LocalContext.current
    val repository = remember { TodoTaskDataAccessorFactory().invoke(context) }
    val color = remember { PreferenceApplier(context).color }

    val tasks = flow?.collectAsLazyPagingItems() ?: return

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tasks.itemCount, { tasks[it]?.id ?: -1 }) {
            val task = tasks[it] ?: return@items
            BoardItem(
                task,
                color,
                repository::insert,
                modify,
                repository::delete,
                Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun BoardItem(
    task: TodoTask,
    color: Int,
    insert: (TodoTask) -> Unit,
    modify: (TodoTask) -> Unit,
    delete: (TodoTask) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf(
        stringResource(id = R.string.modify),
        stringResource(id = R.string.delete)
    )

    var offsetX by remember { mutableFloatStateOf(task.x) }
    var offsetY by remember { mutableFloatStateOf(task.y) }

    Surface(
        shadowElevation = 4.dp,
        color = Color(color),
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
                            insert(task)
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
                        insert(task)
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
                    .drawBehind { drawRect(Color(task.color)) }
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
                            0 -> modify(task)
                            1 -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    delete(task)
                                }
                            }
                        }
                        expanded = false
                    })
                }
            }
        }
    }
}