/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.settings.color.ColorSettingFragment
import jp.toastkid.yobidashi.settings.fragment.BrowserSettingFragment
import jp.toastkid.yobidashi.settings.fragment.ColorFilterSettingFragment
import jp.toastkid.yobidashi.settings.fragment.DisplayingSettingFragment
import jp.toastkid.yobidashi.settings.fragment.EditorSettingFragment
import jp.toastkid.yobidashi.settings.fragment.OtherSettingFragment
import jp.toastkid.yobidashi.settings.fragment.SearchSettingFragment
import jp.toastkid.yobidashi.settings.initial.InitialIndexSettingUseCase
import jp.toastkid.yobidashi.settings.view.screen.BrowserSettingUi
import jp.toastkid.yobidashi.settings.view.screen.ColorFilterSettingUi
import jp.toastkid.yobidashi.settings.view.screen.ColorSettingUi
import jp.toastkid.yobidashi.settings.view.screen.DisplaySettingUi
import jp.toastkid.yobidashi.settings.view.screen.EditorSettingUi
import jp.toastkid.yobidashi.settings.view.screen.OtherSettingUi
import jp.toastkid.yobidashi.settings.view.screen.SearchSettingUi

/**
 * @author toastkidjp
 */
class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activityContext = activity
            ?: return super.onCreateView(inflater, container, savedInstanceState)

        val preferenceApplier = PreferenceApplier(activityContext)
        val initialIndexSettingUseCase = InitialIndexSettingUseCase()

        setHasOptionsMenu(true)

        val pages = listOf(
            DisplayingSettingFragment::class.java to DisplayingSettingFragment,
            ColorSettingFragment::class.java to ColorSettingFragment,
            SearchSettingFragment::class.java to SearchSettingFragment,
            BrowserSettingFragment::class.java to BrowserSettingFragment,
            EditorSettingFragment::class.java to EditorSettingFragment,
            ColorFilterSettingFragment::class.java to ColorFilterSettingFragment,
            OtherSettingFragment::class.java to OtherSettingFragment
        )

        return ComposeViewFactory().invoke(activityContext) {
            MaterialTheme() {
                Column(
                    modifier = Modifier
                        .background(colorResource(id = R.color.setting_background))
                ) {
                    val selectedIndex = remember { mutableStateOf(initialIndexSettingUseCase.extract(arguments)) }
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
                                    text = stringResource(id = page.second.titleId()),
                                    color = Color(preferenceApplier.fontColor),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    SwitchContentWithTabIndex(selectedIndex)
                }
            }
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

}