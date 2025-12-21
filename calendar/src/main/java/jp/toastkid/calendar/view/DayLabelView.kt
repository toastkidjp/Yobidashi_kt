/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.calendar.model.color.CALENDAR_OFF_DAY
import jp.toastkid.calendar.model.color.CALENDAR_SATURDAY
import jp.toastkid.calendar.model.holiday.Holiday
import java.util.Calendar

@Composable
fun DayLabelView(
    date: Int,
    dayOfWeek: Int,
    today: Boolean,
    offDay: Boolean,
    labels: List<Holiday>,
    modifier: Modifier
) {
    Surface(
        color = getSurfaceColor(today),
        modifier = modifier
    ) {
        Column {
            Text(if (date == -1) "" else "$date",
                fontSize = if (labels.isEmpty()) 16.sp else 14.sp,
                textAlign = TextAlign.End,
                color = when {
                    offDay -> CALENDAR_OFF_DAY
                    dayOfWeek == Calendar.SUNDAY -> CALENDAR_OFF_DAY
                    dayOfWeek == Calendar.SATURDAY -> CALENDAR_SATURDAY
                    else -> if (today) Color.White else MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(4.dp)
            )
            labels.forEach {
                Text(
                    "${it.flag} ${it.title}",
                    fontSize = 9.sp,
                    color = CALENDAR_OFF_DAY,
                    lineHeight = 10.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun getSurfaceColor(today: Boolean) =
    if (today) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
