/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.notification.widget.NotificationWidget

/**
 * @author toastkidjp
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            return
        }

        if (PreferenceApplier(context).useNotificationWidget()) {
            NotificationWidget.show(context)
        }
    }
}