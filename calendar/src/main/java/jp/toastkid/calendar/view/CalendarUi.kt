/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.calendar.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.calendar.R
import jp.toastkid.calendar.model.Week
import jp.toastkid.calendar.model.holiday.HolidayCalendar
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import java.util.Calendar
import java.util.GregorianCalendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarUi() {
    val context = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, context)

    val week = arrayOf(
        Calendar.SUNDAY,
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY
    )

    val currentDate = rememberSaveable { mutableStateOf(Calendar.getInstance()) }

    val preferenceApplier = remember { PreferenceApplier(context) }

    val labels = preferenceApplier.usingHolidaysCalendar()
        .mapNotNull {
            HolidayCalendar.findByName(it)
                ?.getHolidays(
                    currentDate.value.get(Calendar.YEAR),
                    currentDate.value.get(Calendar.MONTH) + 1
                )
        }
        .flatten()

    val holidays = HolidayCalendar.findByName(preferenceApplier.usingPrimaryHolidaysCalendar())
        ?.getHolidays(
            currentDate.value.get(Calendar.YEAR),
            currentDate.value.get(Calendar.MONTH) + 1
        ) ?: emptyList()

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.scrollable(rememberScrollState(), Orientation.Vertical)) {
            Row {
                week.forEach { dayOfWeek ->
                    Surface(modifier = Modifier.weight(1f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${getDayOfWeekLabel(dayOfWeek)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (dayOfWeek) {
                                    Calendar.SUNDAY -> OFF_DAY_FG
                                    Calendar.SATURDAY -> SATURDAY_FG
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }

            val firstDay = GregorianCalendar(
                currentDate.value.get(Calendar.YEAR),
                currentDate.value.get(Calendar.MONTH),
                1,
            )

            val weeks = makeMonth(week, firstDay)

            weeks.forEach { w ->
                Row(modifier = Modifier
                    .weight(0.75f)) {
                    w.days().forEach { day ->
                        val isOffDay = holidays.any { it.day == day.date }
                        val candidateLabels = labels.filter { it.day == day.date }
                        DayLabelView(day.date, day.dayOfWeek,
                            isToday(currentDate.value, day.date),
                            isOffDay,
                            candidateLabels,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .combinedClickable(
                                    enabled = day.date != -1,
                                    onClick = {
                                        if (day.date == -1) {
                                            return@combinedClickable
                                        }
                                        openDateArticle(
                                            contentViewModel,
                                            currentDate.value.get(Calendar.YEAR),
                                            currentDate.value.get(Calendar.MONTH),
                                            day.date
                                        )
                                    },
                                    onLongClick = {
                                        if (day.date == -1) {
                                            return@combinedClickable
                                        }
                                        openDateArticle(
                                            contentViewModel,
                                            currentDate.value.get(Calendar.YEAR),
                                            currentDate.value.get(Calendar.MONTH),
                                            day.date,
                                            true
                                        )
                                    }
                                )
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = LocalLifecycleOwner.current, block = {
        contentViewModel.replaceAppBarContent {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Button(onClick = {
                    currentDate.value = GregorianCalendar(
                        currentDate.value.get(Calendar.YEAR),
                        currentDate.value.get(Calendar.MONTH) - 1,
                        1
                    )
                }, modifier = Modifier.padding(8.dp)) {
                    Text("<")
                }

                Surface(modifier = Modifier.padding(8.dp)) {
                    val openYearChooser = remember { mutableStateOf(false) }
                    Box(modifier = Modifier.clickable { openYearChooser.value = true }) {
                        Text("${currentDate.value.get(Calendar.YEAR)}", fontSize = 16.sp)
                        DropdownMenu(
                            expanded = openYearChooser.value,
                            onDismissRequest = { openYearChooser.value = false }) {
                            val years = (1900..2200).toList()
                            Box {
                                val state =
                                    rememberLazyListState(years.indexOf(currentDate.value.get(Calendar.YEAR)))
                                LazyColumn(
                                    state = state,
                                    modifier = Modifier.size(200.dp, 500.dp)
                                ) {
                                    items(years) {
                                        Text(
                                            "${it}",
                                            fontSize = 16.sp,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clickable {
                                                    currentDate.value = GregorianCalendar(
                                                        it,
                                                        currentDate.value.get(Calendar.MONTH),
                                                        1
                                                    )
                                                    openYearChooser.value = false
                                                })
                                    }
                                }
                            }
                        }
                    }
                }

                Text("/", fontSize = 16.sp, modifier = Modifier.padding(8.dp))

                Surface(modifier = Modifier.padding(8.dp)) {
                    val openMonthChooser = remember { mutableStateOf(false) }
                    Box(modifier = Modifier.clickable { openMonthChooser.value = true }) {
                        Text("${currentDate.value.get(Calendar.MONTH) + 1}", fontSize = 16.sp)
                        DropdownMenu(
                            expanded = openMonthChooser.value,
                            onDismissRequest = { openMonthChooser.value = false }) {
                            (1..12).forEach {
                                DropdownMenuItem(
                                    text = { Text("${it}") },
                                    onClick = {
                                        currentDate.value = GregorianCalendar(
                                            currentDate.value.get(Calendar.YEAR),
                                            it - 1,
                                            1
                                        )
                                        openMonthChooser.value = false
                                    })
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        currentDate.value = GregorianCalendar(
                            currentDate.value.get(Calendar.YEAR),
                            currentDate.value.get(Calendar.MONTH) + 1,
                            1
                        )
                    },
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text(">")
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = "Current month",
                    modifier = Modifier.clickable {
                        currentDate.value = Calendar.getInstance()
                    }
                )
            }
        }

        contentViewModel.showAppBar()
    })
}

private fun openDateArticle(
    contentViewModel: ContentViewModel,
    year: Int,
    monthOfYear: Int,
    dayOfMonth: Int,
    background: Boolean = false
) {
    contentViewModel.openDateArticle(year, monthOfYear, dayOfMonth, background)
}

private fun isToday(target: Calendar, date: Int): Boolean {
    val today = Calendar.getInstance()
    return target.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            && target.get(Calendar.MONTH) == today.get(Calendar.MONTH)
            && today.get(Calendar.DAY_OF_MONTH) == date
}

private fun makeMonth(
    week: Array<Int>,
    firstDay: Calendar
): MutableList<Week> {
    var hasStarted1 = false
    var current1 = GregorianCalendar(firstDay.get(Calendar.YEAR), firstDay.get(Calendar.MONTH), firstDay.get(Calendar.DAY_OF_MONTH))

    val weeks = mutableListOf<Week>()
    for (i in 0..5) {
        val w = Week()
        week.forEach { dayOfWeek ->
            if (hasStarted1.not() && dayOfWeek != firstDay.get(Calendar.DAY_OF_WEEK)) {
                w.addEmpty()
                return@forEach
            }
            hasStarted1 = true

            if (firstDay.get(Calendar.MONTH) != current1.get(Calendar.MONTH)) {
                w.addEmpty()
            } else {
                w.add(current1)
            }
            current1.add(Calendar.DAY_OF_MONTH, 1)
        }
        if (w.anyApplicableDate()) {
            weeks.add(w)
        }
    }
    return weeks
}

private fun getDayOfWeekLabel(dayOfWeek: Int) =
    when (dayOfWeek) {
        Calendar.SUNDAY -> "Sun"
        Calendar.MONDAY -> "Mon"
        Calendar.TUESDAY -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY -> "Thu"
        Calendar.FRIDAY -> "Fri"
        Calendar.SATURDAY -> "Sat"
        else -> ""
    }

private val OFF_DAY_FG: Color = Color(190, 50, 55)
private val SATURDAY_FG:  Color = Color(95, 90, 250)
