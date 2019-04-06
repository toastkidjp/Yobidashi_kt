/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.content.Context
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingNotificationBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.notification.widget.NotificationWidget

/**
 * Notification setting fragment.
 *
 * @author toastkidjp
 */
class NotificationSettingFragment : Fragment(), TitleIdSupplier {

    /**
     * View data binding object.
     */
    private lateinit var binding: FragmentSettingNotificationBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting_notification, container, false)
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.useNotificationWidgetCheck.isChecked = preferenceApplier.useNotificationWidget()
        binding.useNotificationWidgetCheck.jumpDrawablesToCurrentState()
    }

    /**
     * Switch notification widget displaying.
     */
    fun switchNotificationWidget() {
        val newState = !preferenceApplier.useNotificationWidget()
        preferenceApplier.setUseNotificationWidget(newState)
        binding.useNotificationWidgetCheck.isChecked = newState

        val activityContext: Context = context ?: return

        @StringRes var messageId: Int = R.string.message_done_showing_notification_widget
        if (newState) {
            NotificationWidget.show(activityContext)
        } else {
            NotificationWidget.hide(activityContext)
            messageId = R.string.message_remove_notification_widget
        }
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    @StringRes
    override fun titleId() = R.string.subhead_notification

}