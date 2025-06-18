/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.markdown.presentation


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.markdown.domain.model.data.TableLine

@Composable
fun TableLineView(line: TableLine, fontSize: TextUnit = 24.sp, modifier: Modifier = Modifier) {
    val viewModel = remember { TableLineViewModel() }

    Column(modifier = modifier) {
        DisableSelection {
            val surfaceColor = MaterialTheme.colorScheme.surface
            Row(modifier = Modifier.fillMaxWidth()
                .drawBehind { drawRect(surfaceColor) }
            ) {
                line.header.forEachIndexed { index, item ->
                    if (index != 0) {
                        VerticalDivider(modifier = Modifier.height(24.dp).padding(vertical = 1.dp))
                    }

                    val headerColumnBackgroundColor = animateColorAsState(
                        if (viewModel.onCursorOnHeader()) MaterialTheme.colorScheme.primary
                        else surfaceColor
                    )

                    Text(
                        item.toString(),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .clickable {
                                viewModel.clickHeaderColumn(index)
                            }
                            .drawBehind { drawRect(headerColumnBackgroundColor.value) }
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 4.dp))

        viewModel.tableData().forEach { itemRow ->
            val cursorOn = remember { mutableStateOf(false) }
            val backgroundColor = animateColorAsState(
                if (cursorOn.value) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            val textColor =
                if (cursorOn.value) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface

            TableRow(itemRow, fontSize, textColor, Modifier.drawBehind { drawRect(backgroundColor.value) })
        }
    }

    LaunchedEffect(line.table) {
        viewModel.start(line.table)
    }
}

@Composable
private fun TableRow(itemRow: List<Any>, fontSize: TextUnit, textColor: Color, modifier: Modifier) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            itemRow.forEachIndexed { index, any ->
                if (index != 0) {
                    VerticalDivider(modifier = Modifier.height(24.dp).padding(vertical = 1.dp))
                }
                Text(
                    any.toString(),
                    color = textColor,
                    fontSize = fontSize,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 4.dp))
    }
}