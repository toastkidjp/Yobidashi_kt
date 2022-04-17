/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.notification.morning.DailyNotificationWorker
import jp.toastkid.yobidashi.notification.widget.NotificationWidget

/**
 * Notification setting fragment.
 *
 * @author toastkidjp
 */
class NotificationSettingFragment : Fragment() {

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var contentViewModel: ContentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activityContext = activity
            ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)

        contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)

        return ComposeViewFactory().invoke(activityContext) {
            val notificationWidgetEnabled =
                remember { mutableStateOf(preferenceApplier.useNotificationWidget()) }
            val morningNotificationEnabled =
                remember { mutableStateOf(preferenceApplier.useDailyNotification()) }

            MaterialTheme {
                Column {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable {
                            switchNotificationWidget()
                            notificationWidgetEnabled.value = preferenceApplier.useNotificationWidget()
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.title_show_notification_widget),
                                modifier = Modifier.fillMaxWidth()
                            )
                            AsyncImage(
                                R.mipmap.thumbnail,
                                contentDescription = stringResource(id = R.string.title_show_notification_widget)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(44.dp)
                        ) {
                            Checkbox(
                                checked = notificationWidgetEnabled.value,
                                onCheckedChange = {},
                                modifier = Modifier.clickable(false) { })
                        }
                    }

                    InsetDivider()

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable {
                            switchDailyNotification()
                            morningNotificationEnabled.value = preferenceApplier.useDailyNotification()
                        }
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.title_show_morning_notification),
                                modifier = Modifier.fillMaxWidth()
                            )
                            AsyncImage(
                                R.mipmap.thumbnail,
                                contentDescription = stringResource(id = R.string.title_show_morning_notification)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(44.dp)
                        ) {
                            Checkbox(
                                checked = morningNotificationEnabled.value,
                                onCheckedChange = {},
                                modifier = Modifier.clickable(false) { })
                        }
                    }
                }
            }
        }
    }

    /**
     * Switch notification widget displaying.
     */
    fun switchNotificationWidget() {
        val newState = !preferenceApplier.useNotificationWidget()
        preferenceApplier.setUseNotificationWidget(newState)

        val activityContext: Context = context ?: return

        @StringRes val messageId =
            if (newState) {
                NotificationWidget.show(activityContext)
                R.string.message_done_showing_notification_widget
            } else {
                NotificationWidget.hide(activityContext)
                R.string.message_remove_notification_widget
            }

        contentViewModel.snackShort(messageId)
    }

    fun switchDailyNotification() {
        val newState = !preferenceApplier.useDailyNotification()
        preferenceApplier.setUseDailyNotification(newState)

        @StringRes val messageId =
            if (newState) R.string.message_stay_tuned
            else R.string.message_remove_notification_widget
        contentViewModel.snackShort(messageId)

        val context = context ?: return
        if (preferenceApplier.useDailyNotification()) {
            DailyNotificationWorker.enqueueNextWork(context)
        } else {
            DailyNotificationWorker.cancelAllWork(context)
        }
    }

    companion object : TitleIdSupplier {

        @StringRes
        override fun titleId() = R.string.subhead_notification

    }
}