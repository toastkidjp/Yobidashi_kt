/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.main.StartUp
import jp.toastkid.yobidashi.settings.PreferencesClearUseCase
import jp.toastkid.yobidashi.settings.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.settings.view.WithIcon

@Composable
internal fun OtherSettingUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = PreferenceApplier(activityContext)
    val contentViewModel = (activityContext as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, it)
    }

    val intentFactory = SettingsIntentFactory()

    val wifiOnly = remember { mutableStateOf(preferenceApplier.wifiOnly) }

    val openConfirmDialog = remember { mutableStateOf(false) }

    val chatApiKeyInput = remember { mutableStateOf(TextFieldValue(preferenceApplier.chatApiKey() ?: "")) }

    Surface(shadowElevation = 4.dp, modifier = Modifier.padding(8.dp)) {
        LazyColumn {
            item {
                Column {
                    TextField(
                        value = chatApiKeyInput.value,
                        onValueChange = { chatApiKeyInput.value = it },
                        label = { Text("Please input Gemini's API Key if you want to use chat function in this app.") },
                        keyboardActions = KeyboardActions(
                            onDone = {
                                preferenceApplier.setChatApiKey(chatApiKeyInput.value.text)
                            }
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    
                    Text(text = "This function is experimental. This API Key will be not send to any sites except Google LLC.")

                    if (chatApiKeyInput.value.text.isNotBlank()) {
                        WithIcon(
                            R.string.title_chat,
                            { contentViewModel?.nextRoute("tool/chat") },
                            MaterialTheme.colorScheme.secondary,
                            R.drawable.ic_chat
                        )
                    }
                }
            }

            item {
                InsetDivider()
            }

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
                        tint = MaterialTheme.colorScheme.secondary
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
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    NewTabSettingItem(
                        StartUp.SEARCH == selected.value,
                        R.string.title_search,
                        R.mipmap.thumbnail_search
                    ) {
                        preferenceApplier.startUp = StartUp.SEARCH.name
                        selected.value = StartUp.SEARCH
                    }

                    NewTabSettingItem(
                        StartUp.BROWSER == selected.value,
                        R.string.title_browser,
                        R.mipmap.thumbnail_browser
                    ) {
                        preferenceApplier.startUp = StartUp.BROWSER.name
                        selected.value = StartUp.BROWSER
                    }

                    NewTabSettingItem(
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
                    MaterialTheme.colorScheme.secondary,
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
                    MaterialTheme.colorScheme.secondary,
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
                    MaterialTheme.colorScheme.secondary,
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
                    MaterialTheme.colorScheme.secondary,
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
                    MaterialTheme.colorScheme.secondary,
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
                    MaterialTheme.colorScheme.secondary,
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
                    MaterialTheme.colorScheme.secondary,
                    R.drawable.ic_close_black
                )
            }

            item {
                Spacer(modifier = Modifier
                    .width(1.dp)
                    .height(48.dp))
            }
        }
    }

    DestructiveChangeConfirmDialog(
        visibleState = openConfirmDialog,
        titleId = R.string.title_clear_settings
    ) {
        PreferencesClearUseCase.make(activityContext).invoke()
    }
}

@Composable
private fun NewTabSettingItem(
    selected: Boolean,
    nameId: Int,
    thumbnailId: Int,
    onClick: () -> Unit
) {
    val selectedColor = MaterialTheme.colorScheme.secondary
    Surface(shadowElevation = 4.dp, modifier = Modifier.clickable(onClick = onClick)) {
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
