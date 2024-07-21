/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.setting.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import jp.toastkid.calendar.model.holiday.HolidayCalendar
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.setting.presentation.CheckableRow

@Composable
internal fun CalendarSettingUi() {
    val preferenceApplier = PreferenceApplier(LocalContext.current)

    val holidayCalendars = remember {
        val map = mutableStateMapOf<HolidayCalendar, MutableState<Boolean>>()
        val usingHolidaysCalendar = preferenceApplier.usingHolidaysCalendar()
        HolidayCalendar.values().forEach {
            map.put(it, mutableStateOf(usingHolidaysCalendar.contains(it.name)))
        }
        map
    }
    val primaryCalendar = remember {
        mutableStateOf(
            HolidayCalendar.findByName(preferenceApplier.usingPrimaryHolidaysCalendar())
        )
    }

    Surface(shadowElevation = 4.dp, modifier = Modifier.padding(8.dp)) {
        LazyColumn {
            item {
                val open = remember { mutableStateOf(false) }
                Box(modifier = Modifier.clickable { open.value = open.value.not() }) {
                    Text("Primary calendar ${primaryCalendar.value?.flag ?: ""}")
                    DropdownMenu(expanded = open.value, onDismissRequest = { open.value = false }) {
                        DropdownMenuItem(
                            text = { Text("Clear") },
                            onClick = {
                                open.value = false
                                preferenceApplier.clearUsingPrimaryHolidaysCalendar()
                                primaryCalendar.value = null
                            }
                        )
                        holidayCalendars.keys.forEach {
                            DropdownMenuItem(
                                text = { Text("${it.flag}") },
                                onClick = {
                                    open.value = false
                                    preferenceApplier.setUsingPrimaryHolidaysCalendar(it.name)
                                    primaryCalendar.value = it
                                }
                            )
                        }
                    }
                }
            }
            items(holidayCalendars.keys.toList()) { holidayCalendar ->
                val booleanState = holidayCalendars.get(holidayCalendar) ?: return@items
                CheckableRow(
                    holidayCalendar.settingTitleId,
                    {
                        holidayCalendars.get(holidayCalendar)?.value = booleanState.value.not()
                    },
                    booleanState,
                    MaterialTheme.colorScheme.secondary
                )
            }

            item {
                Spacer(modifier = Modifier.height(200.dp))
            }
        }

        DisposableEffect(key1 = LocalLifecycleOwner.current, effect = {
            onDispose {
                preferenceApplier.setUsingHolidaysCalendar(
                    holidayCalendars.filter { it.value.value }.map { it.key.name }.joinToString("\t")
                )
            }
        })
    }
}