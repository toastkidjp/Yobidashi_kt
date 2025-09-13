/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.setting.presentation.screen

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.tab.StartUp
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.setting.R
import jp.toastkid.setting.application.intent.SettingsIntentFactory
import jp.toastkid.setting.domain.service.PreferencesClearUseCase
import jp.toastkid.setting.presentation.CheckableRow
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.ui.image.EfficientImage
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.ui.parts.WithIcon

@Composable
internal fun OtherSettingUi() {
    val activityContext = LocalContext.current

    val preferenceApplier = remember { PreferenceApplier(activityContext) }

    val contentViewModel = (activityContext as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, it)
    }

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
                        label = {
                            Text(
                            "Please input Gemini's API Key if you want to use chat function in this app.",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        keyboardActions = KeyboardActions(
                            onDone = {
                                preferenceApplier.setChatApiKey(chatApiKeyInput.value.text)
                                contentViewModel?.snackShort(R.string.message_done_storing_key)
                            }
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    
                    Text(
                        "This function is experimental. This API Key will be not send to any sites except Google LLC.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
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
                        jp.toastkid.lib.R.string.title_search,
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
                        jp.toastkid.web.R.string.title_bookmark,
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
                    R.drawable.ic_settings_cell_black,
                    Modifier.clickable {
                        activityContext.startActivity(SettingsIntentFactory().makeLaunch())
                    }
                )
            }

            item {
                InsetDivider()
            }

            item {
                WithIcon(
                    R.string.title_settings_wifi,
                    R.drawable.ic_wifi_black,
                    Modifier.clickable {
                        activityContext.startActivity(SettingsIntentFactory().wifi())
                    }
                )
            }

            item {
                InsetDivider()
            }

            item {
                WithIcon(
                    R.string.title_settings_wireless,
                    R.drawable.ic_network_black,
                    Modifier.clickable {
                        activityContext.startActivity(SettingsIntentFactory().wireless())
                    }
                )
            }

            item {
                InsetDivider()
            }

            item {
                WithIcon(
                    R.string.title_settings_date_and_time,
                    R.drawable.ic_time,
                    Modifier.clickable {
                        activityContext.startActivity(SettingsIntentFactory().dateAndTime())
                    }
                )
            }

            item {
                InsetDivider()
            }

            item {
                WithIcon(
                    R.string.title_settings_display,
                    R.drawable.ic_phone_android_black,
                    Modifier.clickable {
                        activityContext.startActivity(SettingsIntentFactory().display())
                    }
                )
            }

            item {
                InsetDivider()
            }

            item {
                WithIcon(
                    R.string.title_settings_all_apps,
                    jp.toastkid.search.R.drawable.ic_android_developer,
                    Modifier.clickable {
                        activityContext.startActivity(SettingsIntentFactory().allApps())
                    }
                )
            }

            item {
                InsetDivider()
            }

            item {
                WithIcon(
                    R.string.title_clear_settings,
                    jp.toastkid.lib.R.drawable.ic_close_black,
                    Modifier.clickable {
                        openConfirmDialog.value = true
                    }
                )
            }

            item {
                Spacer(modifier = Modifier
                    .width(1.dp)
                    .height(48.dp))
            }
        }
    }

    if (openConfirmDialog.value) {
        DestructiveChangeConfirmDialog(
            titleId = R.string.title_clear_settings,
            onDismissRequest = { openConfirmDialog.value = false }
        ) {
            PreferencesClearUseCase.make(activityContext).invoke()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val newValue = chatApiKeyInput.value.text
            if (newValue.isNotBlank()) {
                preferenceApplier.setChatApiKey(newValue)
            }
        }
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
        val backgroundColor = if (selected) selectedColor else Color.Transparent
        Column(
            modifier = Modifier
                .drawBehind { drawRect(backgroundColor) }
                .padding(8.dp)
        ) {
            EfficientImage(
                thumbnailId,
                contentDescription = stringResource(id = nameId)
            )
            Text(
                stringResource(id = nameId)
            )
        }
    }
}
