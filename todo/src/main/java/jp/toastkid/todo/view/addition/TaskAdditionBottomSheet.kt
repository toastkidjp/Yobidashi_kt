/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.addition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.todo.R
import jp.toastkid.todo.model.TodoTask
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskEditorUi(
    screenContent: @Composable () -> Unit,
    taskAdditionDialogFragmentViewModel: TaskAdditionDialogFragmentViewModel?,
    onTapAdd: (TodoTask) -> Unit,
    colorPair: ColorPair
) {
    val task = taskAdditionDialogFragmentViewModel?.task?.value
    var descriptionInput by remember { mutableStateOf(task?.description ?: "") }
    var chosenColor by remember { mutableStateOf(Color.Transparent.value.toInt()) }
    task?.let {
        descriptionInput = it.description
        chosenColor = it.color
    }

    val coroutineScope = rememberCoroutineScope()

    val colors = setOf(
        0xffe53935, 0xfff8bbd0, 0xff2196f3, 0xff4caf50, 0xffffeb3b, 0xff3e2723, 0xffffffff
    )

    screenContent()

    if (taskAdditionDialogFragmentViewModel?.bottomSheetScaffoldState?.value != true) {
        return
    }

    Dialog(onDismissRequest = { taskAdditionDialogFragmentViewModel.hide() }) {
        Column {
            val datePickerState = rememberDatePickerState(
                initialDisplayedMonthMillis = System.currentTimeMillis()
            )

            Row {
                TextField(
                    value = descriptionInput,
                    onValueChange = {
                        descriptionInput = it
                        task?.description = descriptionInput
                    },
                    label = { stringResource(id = R.string.description) },
                    singleLine = true,
                    keyboardActions = KeyboardActions {
                        datePickerState.selectedDateMillis?.let {
                            task?.dueDate = it
                        }
                        save(task, onTapAdd)
                        coroutineScope.launch {
                            taskAdditionDialogFragmentViewModel.hide()
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(0.75f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.onSurface
                    ),
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Clear,
                            tint = Color(colorPair.fontColor()),
                            contentDescription = "clear text",
                            modifier = Modifier
                                .offset(x = 8.dp)
                                .clickable {
                                    descriptionInput = ""
                                }
                        )
                    }
                )
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            task?.dueDate = it
                        }
                        save(task, onTapAdd)
                        taskAdditionDialogFragmentViewModel.hide()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = Color.LightGray)
                ) {
                    Text(text = stringResource(id = R.string.add), textAlign = TextAlign.Center)
                }
            }

            Text(
                text = stringResource(id = R.string.color),
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)) {
                colors.forEach { color ->
                    RadioButton(
                        selected = chosenColor == color.toInt(),
                        colors = RadioButtonDefaults
                            .colors(selectedColor = MaterialTheme.colorScheme.secondary),
                        onClick = {
                            task?.color = color.toInt()
                            chosenColor = color.toInt()
                        },
                        modifier = Modifier
                            .width(44.dp)
                            .height(44.dp)
                            .background(Color(color))
                    )
                }
            }

            Text(
                text = "Date",
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            DatePicker(
                state = datePickerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                title = { Text("Due Date") }
            )
        }
    }
}

private fun save(
    task: TodoTask?,
    onTapAdd: (TodoTask) -> Unit
) {
    task?.let {
        updateTask(it)
        onTapAdd(it)
    }
}

private fun updateTask(task: TodoTask?) {
    if (task?.created == 0L) {
        task.created = System.currentTimeMillis()
    }
    task?.lastModified = System.currentTimeMillis()
}
