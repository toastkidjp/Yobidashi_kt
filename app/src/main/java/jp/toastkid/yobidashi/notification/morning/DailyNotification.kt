/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.notification.morning

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class DailyNotification {

    fun show(context: Context) {
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                        ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.title_show_daily_notification)
            notificationManager.createNotificationChannel(makeNotificationChannel(name))
        }
        notificationManager.notify(2, makeNotification(context, RemoteViewsFactory().invoke(context)))
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun makeNotificationChannel(name: String) =
            NotificationChannel(
                    CHANNEL_ID,
                    name,
                    NotificationManager.IMPORTANCE_HIGH
            )

    /**
     * Make notification widget.
     *
     * @param context
     */
    private fun makeNotification(context: Context, contentView: RemoteViews): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_search_white)
                .setCustomContentView(contentView)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
    }

    companion object {
        private const val CHANNEL_ID = "daily"
    }
}