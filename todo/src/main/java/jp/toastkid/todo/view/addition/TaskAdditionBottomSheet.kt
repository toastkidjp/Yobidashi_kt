/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.todo.view.addition

import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.todo.R
import jp.toastkid.todo.model.TodoTask
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun TaskEditorUi(
    screenContent: @Composable() () -> Unit,
    taskAdditionDialogFragmentViewModel: TaskAdditionDialogFragmentViewModel?,
    bottomSheetScaffoldState: ModalBottomSheetState,
    onTapAdd: (TodoTask) -> Unit,
    colorPair: ColorPair
) {
    val task = taskAdditionDialogFragmentViewModel?.task?.observeAsState()?.value
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

    ModalBottomSheetLayout(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        sheetContent = {
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
                        save(task, onTapAdd, bottomSheetScaffoldState)
                        coroutineScope.launch {
                            bottomSheetScaffoldState.hide()
                        }
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colorResource(id = R.color.colorPrimary),
                        backgroundColor = colorResource(id = R.color.soft_background)
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
                        save(task, onTapAdd, bottomSheetScaffoldState)
                        coroutineScope.launch {
                            bottomSheetScaffoldState.hide()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Color(colorPair.bgColor()),
                        contentColor = Color(colorPair.fontColor()),
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

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                factory = { context ->
                    val today = Calendar.getInstance()
                    task?.let {
                        today.timeInMillis = it.dueDate
                    }
                    val datePicker = DatePicker(context)
                    datePicker.init(
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH)
                    ) { _, year, monthOfYear, dayOfMonth ->
                        task?.dueDate = GregorianCalendar(
                            year,
                            monthOfYear,
                            dayOfMonth
                        ).timeInMillis
                    }
                    datePicker
                }
            )
        },
        sheetState = bottomSheetScaffoldState
    ) {
        screenContent()
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun save(
    task: TodoTask?,
    onTapAdd: (TodoTask) -> Unit,
    bottomSheetScaffoldState: ModalBottomSheetState
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
