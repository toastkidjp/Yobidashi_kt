/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import android.content.Context
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.main.StartUp
import jp.toastkid.yobidashi.settings.PreferencesClearUseCase
import jp.toastkid.yobidashi.settings.view.WithIcon

@Composable
internal fun OtherSettingUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = PreferenceApplier(activityContext)
    val iconTint = Color(IconColorFinder.from(activityContext).invoke())

    val intentFactory = SettingsIntentFactory()

    val wifiOnly = remember { mutableStateOf(preferenceApplier.wifiOnly) }

    val openConfirmDialog = remember { mutableStateOf(false) }

    MaterialTheme() {
        Surface(elevation = 4.dp, modifier = Modifier.padding(16.dp)) {
            LazyColumn(
                Modifier
                    .background(colorResource(id = R.color.setting_background))
                    .nestedScroll(rememberViewInteropNestedScrollConnection())
            ) {
                item {
                    jp.toastkid.yobidashi.settings.view.CheckableRow(
                        R.string.title_wifi_only,
                        {
                            val newState = !preferenceApplier.wifiOnly
                            preferenceApplier.wifiOnly = newState
                            wifiOnly.value = preferenceApplier.wifiOnly
                        },
                        wifiOnly
                    )
                }

                item {
                    InsetDivider()
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.ic_tab_black),
                            contentDescription = stringResource(id = R.string.title_setting_new_tab),
                            tint = iconTint
                        )
                        Text(
                            stringResource(id = R.string.title_setting_new_tab),
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                item {
                    val selected =
                        remember { mutableStateOf(StartUp.findByName(preferenceApplier.startUp)) }
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        NewTabSettingItem(
                            preferenceApplier.color,
                            StartUp.SEARCH == selected.value,
                            R.string.title_search,
                            R.mipmap.thumbnail_search
                        ) {
                            preferenceApplier.startUp = StartUp.SEARCH.name
                            selected.value = StartUp.SEARCH
                        }

                        NewTabSettingItem(
                            preferenceApplier.color,
                            StartUp.BROWSER == selected.value,
                            R.string.title_browser,
                            R.mipmap.thumbnail_browser
                        ) {
                            preferenceApplier.startUp = StartUp.BROWSER.name
                            selected.value = StartUp.BROWSER
                        }

                        NewTabSettingItem(
                            preferenceApplier.color,
                            StartUp.BOOKMARK == selected.value,
                            R.string.title_bookmark,
                            R.mipmap.thumbnail_bookmark
                        ) {
                            preferenceApplier.startUp = StartUp.BOOKMARK.name
                            selected.value = StartUp.BOOKMARK
                        }
                    }
                }

                item {
                    InsetDivider()
                }

                item {
                    WithIcon(
                        R.string.title_settings_device,
                        { activityContext.startActivity(intentFactory.makeLaunch()) },
                        iconTint,
                        R.drawable.ic_settings_cell_black
                    )
                }

                item {
                    InsetDivider()
                }

                item {
                    WithIcon(
                        R.string.title_settings_wifi,
                        { activityContext.startActivity(intentFactory.wifi()) },
                        iconTint,
                        R.drawable.ic_wifi_black
                    )
                }

                item {
                    InsetDivider()
                }

                item {
                    WithIcon(
                        R.string.title_settings_wireless,
                        {
                            activityContext.startActivity(intentFactory.wireless())
                        },
                        iconTint,
                        R.drawable.ic_network_black
                    )
                }

                item {
                    InsetDivider()
                }

                item {
                    WithIcon(
                        R.string.title_settings_date_and_time,
                        {
                            activityContext.startActivity(intentFactory.dateAndTime())
                        },
                        iconTint,
                        R.drawable.ic_time
                    )
                }

                item {
                    InsetDivider()
                }

                item {
                    WithIcon(
                        R.string.title_settings_display,
                        { activityContext.startActivity(intentFactory.display()) },
                        iconTint,
                        R.drawable.ic_phone_android_black
                    )
                }

                item {
                    InsetDivider()
                }

                item {
                    WithIcon(
                        R.string.title_settings_all_apps,
                        { activityContext.startActivity(intentFactory.allApps()) },
                        iconTint,
                        R.drawable.ic_android_developer
                    )
                }

                item {
                    InsetDivider()
                }

                item {
                    WithIcon(
                        R.string.title_clear_settings,
                        { openConfirmDialog.value = true },
                        iconTint,
                        R.drawable.ic_close_black
                    )
                }
            }
        }
    }

    DestructiveChangeConfirmDialog(
        visibleState = openConfirmDialog,
        titleId = R.string.title_clear_settings
    ) {
        clearSettings(activityContext)
    }
}

@Composable
private fun NewTabSettingItem(
    @ColorInt color: Int,
    selected: Boolean,
    nameId: Int,
    thumbnailId: Int, onClick: () -> Unit
) {
    val selectedColor = Color(color)
    Surface(elevation = 4.dp, modifier = Modifier.clickable(onClick = onClick)) {
        Column(
            modifier = Modifier
                .background(if (selected) selectedColor else Color.Transparent)
                .padding(8.dp)
        ) {
            AsyncImage(
                thumbnailId,
                contentDescription = stringResource(id = nameId)
            )
            Text(
                stringResource(id = nameId)
            )
        }
    }
}

/**
 * Clear all settings.
 */
private fun clearSettings(context: Context) {
    val activity = context as? FragmentActivity ?: return

    activity.supportFragmentManager.setFragmentResultListener(
        "clear_setting",
        activity,
        { key, result ->
            PreferencesClearUseCase.make(activity).invoke()
        }
    )
}

