/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.sort

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import jp.toastkid.article_viewer.R

@Composable
internal fun SortSettingDialogUi(
    onDismissRequest: () -> Unit,
    onSelect: (Sort) -> Unit,
    currentSortName: String,
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = stringResource(id = R.string.title_sort_setting),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )

                val currentSort = Sort.findByName(currentSortName)
                Sort.entries.forEach { sort ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable {
                                onSelect(sort)
                                onDismissRequest()
                            }
                    ) {
                        RadioButton(
                            selected = sort == currentSort,
                            colors = RadioButtonDefaults
                                .colors(selectedColor = MaterialTheme.colorScheme.secondary),
                            onClick = {
                                onSelect(sort)
                                onDismissRequest()
                            }
                        )
                        Text(sort.name)
                    }
                }
            }
        }
    }
}