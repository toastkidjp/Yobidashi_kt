/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.settings.initial.InitialIndexSettingUseCase

@Composable
fun SettingTopUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = PreferenceApplier(activityContext)
    val initialIndexSettingUseCase = InitialIndexSettingUseCase()

    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.setting_background))
    ) {
        val selectedIndex = remember { mutableStateOf(0) }

        val pages = arrayOf(
            R.string.subhead_displaying,
            R.string.title_settings_color,
            R.string.search,
            R.string.subhead_browser,
            R.string.subhead_editor,
            R.string.title_color_filter,
            R.string.subhead_others
        )

        ScrollableTabRow(
            backgroundColor = Color(preferenceApplier.color),
            selectedTabIndex = selectedIndex.value,
            edgePadding = 4.dp,
            divider = {
                Divider(
                    color = Color(preferenceApplier.fontColor),
                    thickness = 1.dp
                )
            },
            modifier = Modifier.height(44.dp)
        ) {
            pages.forEachIndexed { index, page ->
                Tab(
                    selected = selectedIndex.value == index,
                    onClick = {
                        selectedIndex.value = index
                    },
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                ) {
                    Text(
                        text = stringResource(id = page),
                        color = Color(preferenceApplier.fontColor),
                        fontSize = 16.sp
                    )
                }
            }
        }

        SwitchContentWithTabIndex(selectedIndex)
    }
}


@Composable
private fun SwitchContentWithTabIndex(selectedIndex: MutableState<Int>) {
    when (selectedIndex.value) {
        0 -> DisplaySettingUi()
        1 -> ColorSettingUi()
        2 -> SearchSettingUi()
        3 -> BrowserSettingUi()
        4 -> EditorSettingUi()
        5 -> ColorFilterSettingUi()
        6 -> OtherSettingUi()
        else -> DisplaySettingUi()
    }
}

/*

override fun onDetach() {
    activity?.let {
        ViewModelProvider(it).get(ContentViewModel::class.java).refresh()
    }
    super.onDetach()
}

fun setFrom(javaClass: Class<Fragment>?) {
    if (arguments == null) {
        arguments = Bundle()
    }

    InitialIndexSettingUseCase().put(arguments, javaClass)
}
*/
