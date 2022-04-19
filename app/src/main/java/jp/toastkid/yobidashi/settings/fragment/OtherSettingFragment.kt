/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import jp.toastkid.lib.color.IconColorFinder
import jp.toastkid.lib.dialog.ConfirmDialogFragment
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.scroll.rememberViewInteropNestedScrollConnection
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.main.StartUp
import jp.toastkid.yobidashi.settings.PreferencesClearUseCase
import jp.toastkid.yobidashi.settings.view.CheckableRow
import jp.toastkid.yobidashi.settings.view.WithIcon

/**
 * @author toastkidjp
 */
class OtherSettingFragment : Fragment() {

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private val intentFactory = SettingsIntentFactory()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)

        return ComposeViewFactory().invoke(activityContext) {
            val iconTint = Color(IconColorFinder.from(activityContext).invoke())

            val wifiOnly = remember { mutableStateOf(preferenceApplier.wifiOnly) }

            MaterialTheme() {
                Surface(elevation = 4.dp, modifier = Modifier.padding(16.dp)) {
                    LazyColumn(
                        Modifier
                            .background(colorResource(id = R.color.setting_background))
                            .nestedScroll(rememberViewInteropNestedScrollConnection())
                    ) {
                        item {
                            CheckableRow(
                                R.string.title_wifi_only,
                                {
                                    switchWifiOnly()
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
                            val selected = remember { mutableStateOf(StartUp.findByName(preferenceApplier.startUp)) }
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
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
                                { deviceSetting() },
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
                                { wifi() },
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
                                { wireless() },
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
                                { dateAndTime() },
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
                                { display() },
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
                                { allApps() },
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
                                { clearSettings() },
                                iconTint,
                                R.drawable.ic_close_black
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NewTabSettingItem(selected: Boolean, nameId: Int, thumbnailId: Int, onClick: () -> Unit) {
        val selectedColor = Color(preferenceApplier.color)
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
     * Switch Wi-Fi only mode.
     */
    /**
     * Switch Wi-Fi only mode.
     */
    fun switchWifiOnly() {
        val newState = !preferenceApplier.wifiOnly
        preferenceApplier.wifiOnly = newState
    }


    /**
     * Clear all settings.
     */
    /**
     * Clear all settings.
     */
    fun clearSettings() {
        parentFragmentManager.setFragmentResultListener(
            "clear_setting",
            this,
            { key, result ->
                activity?.let {
                    PreferencesClearUseCase(it).invoke()
                }
            }
        )

        ConfirmDialogFragment.show(
            parentFragmentManager,
            getString(R.string.title_clear_settings),
            Html.fromHtml(
                getString(R.string.confirm_clear_all_settings),
                Html.FROM_HTML_MODE_COMPACT
            ),
            "clear_setting"
        )
    }

    /**
     * Call device settings.
     */
    /**
     * Call device settings.
     */
    fun deviceSetting() {
        startActivity(intentFactory.makeLaunch())
    }

    /**
     * Call Wi-Fi settings.
     */
    /**
     * Call Wi-Fi settings.
     */
    fun wifi() {
        startActivity(intentFactory.wifi())
    }

    /**
     * Call Wireless settings.
     */
    /**
     * Call Wireless settings.
     */
    fun wireless() {
        startActivity(intentFactory.wireless())
    }

    /**
     * Call Date-and-Time settings.
     */
    /**
     * Call Date-and-Time settings.
     */
    fun dateAndTime() {
        startActivity(intentFactory.dateAndTime())
    }

    /**
     * Call display settings.
     */
    /**
     * Call display settings.
     */
    fun display() {
        startActivity(intentFactory.display())
    }

    /**
     * Call all app settings.
     */
    /**
     * Call all app settings.
     */
    fun allApps() {
        startActivity(intentFactory.allApps())
    }

    override fun onDetach() {
        parentFragmentManager.clearFragmentResultListener("clear_setting")
        super.onDetach()
    }

    companion object : TitleIdSupplier {

        @StringRes
        override fun titleId() = R.string.subhead_others

    }
}