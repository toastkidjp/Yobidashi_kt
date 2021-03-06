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
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingNotificationBinding
import jp.toastkid.yobidashi.notification.morning.DailyNotificationWorker
import jp.toastkid.yobidashi.notification.widget.NotificationWidget

/**
 * Notification setting fragment.
 *
 * @author toastkidjp
 */
class NotificationSettingFragment : Fragment() {

    /**
     * View data binding object.
     */
    private lateinit var binding: FragmentSettingNotificationBinding

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
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this
        val activityContext = activity
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)

        contentViewModel = ViewModelProvider(activityContext).get(ContentViewModel::class.java)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.useNotificationWidgetCheck.isChecked = preferenceApplier.useNotificationWidget()
        binding.useNotificationWidgetCheck.jumpDrawablesToCurrentState()
        binding.useDailyNotificationCheck.isChecked = preferenceApplier.useDailyNotification()
        binding.useDailyNotificationCheck.jumpDrawablesToCurrentState()
    }

    /**
     * Switch notification widget displaying.
     */
    fun switchNotificationWidget() {
        val newState = !preferenceApplier.useNotificationWidget()
        preferenceApplier.setUseNotificationWidget(newState)
        binding.useNotificationWidgetCheck.isChecked = newState

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
        binding.useDailyNotificationCheck.isChecked = newState

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

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_setting_notification

        @StringRes
        override fun titleId() = R.string.subhead_notification

    }
}