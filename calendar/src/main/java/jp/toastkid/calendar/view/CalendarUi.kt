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
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarUi() {
    val context = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    val viewModel = remember { CalendarViewModel() }
    val preferenceApplier = remember { PreferenceApplier(context) }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.scrollable(rememberScrollState(), Orientation.Vertical)) {
            Row {
                viewModel.week().forEach { dayOfWeek ->
                    Surface(modifier = Modifier.weight(1f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                viewModel.getDayOfWeekLabel(dayOfWeek),
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

            val labels = preferenceApplier.usingHolidaysCalendar()
                .flatMap {
                    viewModel.calculateHolidays(it)
                }

            val holidays = viewModel.calculateHolidays(preferenceApplier.usingPrimaryHolidaysCalendar())

            val weeks = viewModel.makeMonth()

            weeks.forEach { w ->
                Row(modifier = Modifier
                    .weight(0.75f)) {
                    w.days().forEach { day ->
                        val isOffDay = holidays.any { it.day == day.date }
                        val candidateLabels = labels.filter { it.day == day.date }
                        DayLabelView(day.date, day.dayOfWeek,
                            viewModel.isToday(day.date),
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
                                        viewModel.openDateArticle(
                                            contentViewModel,
                                            day.date
                                        )
                                    },
                                    onLongClick = {
                                        if (day.date == -1) {
                                            return@combinedClickable
                                        }
                                        viewModel.openDateArticle(
                                            contentViewModel,
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
                    viewModel.moveMonth(-1)
                }, modifier = Modifier.padding(8.dp)) {
                    Text("<")
                }

                Surface(modifier = Modifier.padding(8.dp)) {
                    val openYearChooser = remember { mutableStateOf(false) }
                    Box(modifier = Modifier.clickable { openYearChooser.value = true }) {
                        Text(viewModel.currentYearLabel(), fontSize = 16.sp)
                        DropdownMenu(
                            expanded = openYearChooser.value,
                            onDismissRequest = { openYearChooser.value = false }) {
                            val years = (1900..2200).toList()
                            Box {
                                val state =
                                    rememberLazyListState(years.indexOf(viewModel.year()))
                                LazyColumn(
                                    state = state,
                                    modifier = Modifier.size(200.dp, 500.dp)
                                ) {
                                    items(years) {
                                        Text(
                                            "$it",
                                            fontSize = 16.sp,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .clickable {
                                                    viewModel.setYear(it)
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
                        Text(viewModel.currentMonthLabel(), fontSize = 16.sp)
                        DropdownMenu(
                            expanded = openMonthChooser.value,
                            onDismissRequest = { openMonthChooser.value = false }) {
                            (1..12).forEach {
                                DropdownMenuItem(
                                    text = { Text("${it}") },
                                    onClick = {
                                        viewModel.setMonth(it)
                                        openMonthChooser.value = false
                                    })
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.moveMonth(1)
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
                        viewModel.moveToCurrentMonth()
                    }
                )
            }
        }

        contentViewModel.showAppBar()
    })
}

private val OFF_DAY_FG: Color = Color(190, 50, 55)
private val SATURDAY_FG:  Color = Color(95, 90, 250)
