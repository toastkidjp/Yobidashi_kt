/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.setting.presentation.screen

import android.webkit.CookieManager
import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.setting.R
import jp.toastkid.setting.presentation.CheckableRow
import jp.toastkid.setting.presentation.TextMenu
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.web.user_agent.UserAgentDropdown
import kotlin.math.roundToInt

@Composable
internal fun WebSettingUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = remember { PreferenceApplier(activityContext) }
    val contentViewModel = (activityContext as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, activityContext)
    }

    val retainTabs = remember { mutableStateOf(preferenceApplier.doesRetainTabs()) }
    val homeUrl = remember { mutableStateOf(preferenceApplier.homeUrl) }
    val useJavaScript = remember { mutableStateOf(preferenceApplier.useJavaScript()) }
    val useImage = remember { mutableStateOf(preferenceApplier.doesLoadImage()) }
    val saveFormData = remember { mutableStateOf(preferenceApplier.doesSaveForm()) }
    val saveViewHistory = remember { mutableStateOf(preferenceApplier.saveViewHistory) }
    val userAgent = remember { mutableStateOf(preferenceApplier.userAgent()) }
    val poolSize = remember { mutableFloatStateOf(preferenceApplier.poolSize.toFloat() / 30f) }
    val backgroundAlpha = remember { mutableStateOf(preferenceApplier.getWebViewBackgroundAlpha()) }
    val useDarkMode = remember { mutableStateOf(preferenceApplier.useDarkMode()) }
    val useAdRemover = remember { mutableStateOf(preferenceApplier.adRemove) }

    Surface(shadowElevation = 4.dp, modifier = Modifier.padding(8.dp)) {
        LazyColumn {
            item {
                CheckableRow(
                    R.string.title_enable_javascript,
                    {
                        val newState = !preferenceApplier.doesRetainTabs()
                        preferenceApplier.setRetainTabs(newState)
                        @StringRes val messageId: Int = if (newState)
                            R.string.message_check_retain_tabs
                        else
                            R.string.message_check_doesnot_retain_tabs
                        contentViewModel?.snackShort(messageId)
                        retainTabs.value = preferenceApplier.doesRetainTabs()
                    },
                    retainTabs,
                    MaterialTheme.colorScheme.secondary,
                    R.drawable.ic_tab_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_home_black),
                        tint = MaterialTheme.colorScheme.secondary,
                        contentDescription = stringResource(id = R.string.title_retain_tabs)
                    )
                    TextField(
                        value = homeUrl.value,
                        onValueChange = {
                            homeUrl.value = it
                        },
                        label = { stringResource(id = R.string.title_home) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (homeUrl.value.isEmpty()) {
                                contentViewModel?.snackShort(
                                    jp.toastkid.lib.R.string.favorite_search_addition_dialog_empty_message
                                )
                                return@KeyboardActions
                            }
                            if (Urls.isInvalidUrl(homeUrl.value)) {
                                contentViewModel?.snackShort(
                                    R.string.message_invalid_url
                                )
                                return@KeyboardActions
                            }
                            preferenceApplier.homeUrl = homeUrl.value

                            contentViewModel?.snackShort(
                                activityContext.getString(
                                    R.string.message_commit_home,
                                    homeUrl.value
                                )
                            )
                        }),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    R.string.title_enable_javascript,
                    {
                        val newState = !preferenceApplier.useJavaScript()
                        preferenceApplier.setUseJavaScript(newState)
                        @StringRes val messageId: Int = if (newState)
                            R.string.message_js_enabled
                        else
                            R.string.message_js_disabled
                        contentViewModel?.snackShort(messageId)
                        useJavaScript.value = preferenceApplier.useJavaScript()
                    },
                    useJavaScript
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    R.string.title_load_image,
                    {
                        val newState = !preferenceApplier.doesLoadImage()
                        preferenceApplier.setLoadImage(newState)
                        useImage.value = preferenceApplier.doesLoadImage()
                    },
                    useImage,
                    MaterialTheme.colorScheme.secondary,
                    R.drawable.ic_image
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    R.string.title_save_form_data,
                    {
                        val newState = !preferenceApplier.doesSaveForm()
                        preferenceApplier.setSaveForm(newState)
                        saveFormData.value = preferenceApplier.doesSaveForm()
                    },
                    saveFormData
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    R.string.title_save_view_history,
                    {
                        val newState = !preferenceApplier.saveViewHistory
                        preferenceApplier.saveViewHistory = newState
                        saveViewHistory.value = preferenceApplier.doesSaveForm()
                    },
                    saveViewHistory,
                    MaterialTheme.colorScheme.secondary,
                    R.drawable.ic_open_in_browser_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                val open = remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable(onClick = {
                            open.value = true
                        })
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_user_agent_black),
                        tint = MaterialTheme.colorScheme.secondary,
                        contentDescription = stringResource(id = jp.toastkid.web.R.string.title_user_agent)
                    )

                    Text(
                        stringResource(id = jp.toastkid.web.R.string.title_user_agent),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )

                    Text(
                        userAgent.value,
                        modifier = Modifier.wrapContentWidth()
                    )

                    UserAgentDropdown(open.value, { open.value = false }) {
                        preferenceApplier.setUserAgent(it.name)
                        userAgent.value = preferenceApplier.userAgent()
                    }
                }
            }

            item {
                InsetDivider()
            }

            item {
                Column(
                    modifier = Modifier
                        .height(56.dp)
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    Row() {
                        Text(stringResource(id = R.string.title_tab_retaining_size))
                        Text(
                            "${((30) * poolSize.floatValue).roundToInt()}",
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                        )
                    }
                    Slider(
                        value = poolSize.floatValue,
                        onValueChange = {
                            val newValue = ((30) * it).roundToInt()
                            poolSize.floatValue = it
                            preferenceApplier.poolSize = newValue
                        },
                        steps = 30,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            item {
                InsetDivider()
            }

            item {
                Column(
                    modifier = Modifier
                        .height(56.dp)
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    Row {
                        Text("Background alpha")
                        Text(
                            "${backgroundAlpha.value}",
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                        )
                    }
                    Slider(
                        value = backgroundAlpha.value,
                        onValueChange = {
                            backgroundAlpha.value = it
                            preferenceApplier.setWebViewBackgroundAlpha(it)
                        },
                        steps = 100,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    R.string.title_dark_mode,
                    {
                        val newState = !preferenceApplier.useDarkMode()
                        preferenceApplier.setUseDarkMode(newState)
                        useDarkMode.value = preferenceApplier.useDarkMode()
                    },
                    useDarkMode,
                    MaterialTheme.colorScheme.secondary,
                    R.drawable.ic_dark_mode_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                CheckableRow(
                    R.string.title_remove_ad,
                    {
                        val newState = !preferenceApplier.adRemove
                        preferenceApplier.adRemove = newState
                        useAdRemover.value = preferenceApplier.adRemove
                    },
                    useAdRemover,
                    MaterialTheme.colorScheme.secondary,
                    R.drawable.ic_block_black
                )
            }

            item {
                InsetDivider()
            }

            item {
                TextMenu(R.string.title_clear_cache) {
                    WebView(activityContext).clearCache(true)
                    contentViewModel?.snackShort(jp.toastkid.lib.R.string.done_clear)
                }
            }

            item {
                InsetDivider()
            }

            item {
                TextMenu(R.string.title_clear_form_data) {
                    WebView(activityContext).clearFormData()
                    contentViewModel?.snackShort(jp.toastkid.lib.R.string.done_clear)
                }
            }

            item {
                InsetDivider()
            }

            item {
                TextMenu(R.string.title_clear_coolie) {
                    CookieManager.getInstance().removeAllCookies {
                        contentViewModel?.snackShort(jp.toastkid.lib.R.string.done_clear)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.width(1.dp).height(32.dp))
            }
        }
    }
}
