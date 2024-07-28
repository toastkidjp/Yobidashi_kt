/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.date

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import jp.toastkid.article_viewer.calendar.DateSelectedActionUseCase
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateFilterDialogUi(
    onDismissRequest: () -> Unit,
    dateSelectedActionUseCase: DateSelectedActionUseCase
) {
    var year by remember { mutableStateOf(0) }
    var monthOfYear by remember { mutableStateOf(0) }
    var dayOfMonth by remember { mutableStateOf(0) }
    val datePickerState = rememberDatePickerState()
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(shadowElevation = 4.dp) {
            Column {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Button(
                    onClick = {
                        onDismissRequest()
                        val selectedDateMillis = datePickerState.selectedDateMillis ?: return@Button
                        val calendar = Calendar.getInstance().also {
                            it.timeInMillis = selectedDateMillis
                        }
                        dateSelectedActionUseCase.invoke(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = Color.LightGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = jp.toastkid.lib.R.string.open)
                    )
                }
            }
        }
    }
}