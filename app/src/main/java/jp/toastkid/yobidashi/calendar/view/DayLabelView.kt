/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.calendar.view

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
import jp.toastkid.yobidashi.calendar.model.holiday.Holiday
import java.util.Calendar

@Composable
fun DayLabelView(
    date: Int,
    dayOfWeek: Int,
    today: Boolean,
    candidateHolidays: List<Holiday>,
    modifier: Modifier
) {
    val offDay = candidateHolidays.isNotEmpty()

    Surface(
        color = getSurfaceColor(today),
        modifier = modifier
    ) {
        Column {
            Text(if (date == -1) "" else "$date",
                fontSize = if (candidateHolidays.isEmpty()) 16.sp else 14.sp,
                textAlign = TextAlign.Start,
                color = when (dayOfWeek) {
                    Calendar.SUNDAY -> OFF_DAY_FG
                    Calendar.SATURDAY -> SATURDAY_FG
                    else -> if (offDay) OFF_DAY_FG else if (today) Color.White else MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(4.dp)
            )
            candidateHolidays.forEach {
                Text(it.title, fontSize = 9.sp, color = OFF_DAY_FG, softWrap = false,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun getSurfaceColor(today: Boolean) =
    if (today) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface

private val OFF_DAY_FG:  Color = Color(220, 50, 55)
private val SATURDAY_FG:  Color = Color(95, 90, 250)
