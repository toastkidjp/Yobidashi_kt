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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
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
import jp.toastkid.calendar.model.holiday.Holiday
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarUi() {
    val context = LocalContext.current as? ComponentActivity ?: return
    val contentViewModel = viewModel(ContentViewModel::class.java, context)
    val viewModel = remember { CalendarViewModel() }
    val preferenceApplier = remember { PreferenceApplier(context) }

    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(viewModel.calculateInitialPage()) { 4000 * 12 }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        shadowElevation = 4.dp
    ) {
        HorizontalPager(state = pagerState) {
            val calendar = viewModel.fromPage(pagerState.currentPage)
            MonthCalendar(
                viewModel.week(),
                viewModel.makeMonth(calendar),
                preferenceApplier.usingHolidaysCalendar()
                    .flatMap { viewModel.calculateHolidays(calendar, it) },
                viewModel.calculateHolidays(calendar, preferenceApplier.usingPrimaryHolidaysCalendar()),
                { date, onBackground -> viewModel.openDateArticle(contentViewModel, pagerState.currentPage, date, onBackground) },
                { viewModel.isToday(calendar, it) }
            ) { viewModel.getDayOfWeekLabel(it) }
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
                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }, modifier = Modifier.padding(8.dp)) {
                    Text("<")
                }

                Surface(modifier = Modifier.padding(8.dp)) {
                    val openYearChooser = remember { mutableStateOf(false) }
                    Box(modifier = Modifier.clickable { openYearChooser.value = true }) {
                        Text(viewModel.currentYearLabel(pagerState.currentPage), fontSize = 16.sp)
                        DropdownMenu(
                            expanded = openYearChooser.value,
                            onDismissRequest = { openYearChooser.value = false }) {
                            val years = (1900..2200).toList()
                            Box {
                                val state =
                                    rememberLazyListState(years.indexOf(viewModel.year(pagerState.currentPage)))
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
                                                    val fromPage =
                                                        viewModel.fromPage(pagerState.currentPage)
                                                    fromPage.set(Calendar.YEAR, it)
                                                    coroutineScope.launch {
                                                        pagerState.animateScrollToPage(
                                                            viewModel.toPage(fromPage)
                                                        )
                                                    }
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
                        Text(viewModel.currentMonthLabel(pagerState.currentPage), fontSize = 16.sp)
                        DropdownMenu(
                            expanded = openMonthChooser.value,
                            onDismissRequest = { openMonthChooser.value = false }) {
                            (1..12).forEach {
                                DropdownMenuItem(
                                    text = { Text("${it}") },
                                    onClick = {
                                        coroutineScope.launch {
                                            val fromPage =
                                                viewModel.fromPage(pagerState.currentPage)
                                            fromPage.set(Calendar.MONTH, it - 1)
                                            pagerState.animateScrollToPage(
                                                viewModel.toPage(fromPage)
                                            )
                                        }
                                        openMonthChooser.value = false
                                    })
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
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
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(viewModel.calculateInitialPage())
                        }
                    }
                )
            }
        }

        contentViewModel.showAppBar()
    })
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MonthCalendar(
    week: Array<Int>,
    month: List<Week>,
    labels: List<Holiday>,
    holidays: List<Holiday>,
    openDateArticle: (Int, Boolean) -> Unit,
    isToday: (Int) -> Boolean,
    getDayOfWeekLabel: (Int) -> String,
) {
    Column(modifier = Modifier.scrollable(rememberScrollState(), Orientation.Vertical)) {
        Row {
            week.forEach { dayOfWeek ->
                Surface(modifier = Modifier.weight(1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            getDayOfWeekLabel(dayOfWeek),
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

        month.forEach { w ->
            Row(
                modifier = Modifier
                    .weight(0.75f)
            ) {
                w.days().forEach { day ->
                    val isOffDay = holidays.any { it.day == day.date }
                    val candidateLabels = labels.filter { it.day == day.date }
                    DayLabelView(day.date, day.dayOfWeek,
                        isToday(day.date),
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
                                        day.date,
                                        false
                                    )
                                },
                                onLongClick = {
                                    if (day.date == -1) {
                                        return@combinedClickable
                                    }
                                    openDateArticle(
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

private val OFF_DAY_FG: Color = Color(190, 50, 55)
private val SATURDAY_FG:  Color = Color(95, 90, 250)
