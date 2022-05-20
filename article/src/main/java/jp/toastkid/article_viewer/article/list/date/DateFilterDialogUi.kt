/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.date

import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.calendar.DateSelectedActionUseCase
import jp.toastkid.lib.preference.ColorPair
import java.util.Calendar

@Composable
internal fun DateFilterDialogUi(
    colorPair: ColorPair,
    openDialog: MutableState<Boolean>,
    dateSelectedActionUseCase: DateSelectedActionUseCase
) {
    var year by remember { mutableStateOf(0) }
    var monthOfYear by remember { mutableStateOf(0) }
    var dayOfMonth by remember { mutableStateOf(0) }
    Dialog(
        onDismissRequest = {
            openDialog.value = false
        }
    ) {
        Surface(elevation = 4.dp) {
            Column {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    factory = { context ->
                        val today = Calendar.getInstance()
                        val datePicker = DatePicker(context)
                        datePicker.init(
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH)
                        ) { _, y, m, d ->
                            year = y
                            monthOfYear = m
                            dayOfMonth = d
                        }
                        datePicker
                    }
                )

                Button(
                    onClick = {
                        openDialog.value = false
                        dateSelectedActionUseCase.invoke(
                            year,
                            monthOfYear,
                            dayOfMonth
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
                        disabledContentColor = Color.LightGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.open)
                    )
                }
            }
        }
    }
}