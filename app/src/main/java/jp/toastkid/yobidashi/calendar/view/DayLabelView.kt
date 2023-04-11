/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.view


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun DayLabelView(date: Int, dayOfWeek: Int, offDay: Boolean, today: Boolean, modifier: Modifier) {
    Surface(
        color = if (today) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else if (offDay || dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(if (date == -1) "" else "$date", fontSize = 16.sp, color = when (dayOfWeek) {
                Calendar.SUNDAY -> OFF_DAY_FG
                Calendar.SATURDAY -> SATURDAY_FG
                else -> if (offDay) OFF_DAY_FG else if (today) Color.White else MaterialTheme.colorScheme.onSurface
            })
        }
    }
}

private val OFF_DAY_FG:  Color = Color(220, 50, 55)
private val SATURDAY_FG:  Color = Color(55, 50, 190)
private val DAY_BG: Color = Color(250, 250, 255)