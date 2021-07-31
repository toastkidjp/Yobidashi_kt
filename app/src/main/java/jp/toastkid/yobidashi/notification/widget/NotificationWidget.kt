package jp.toastkid.yobidashi.notification.widget

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import jp.toastkid.yobidashi.R

/**
 * Notification widget's launcher.
 *
 * @author toastkidjp
 */
object NotificationWidget {

    /**
     * Notification ID.
     */
    private const val ID = 1

    /**
     * Notification channel ID.
     */
    private const val CHANNEL_ID = "notification_widget"

    /**
     * Show notification widget.
     *
     * @param context
     */
    fun show(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.title_show_notification_widget)
            notificationManager.createNotificationChannel(makeNotificationChannel(name))
        }
        notificationManager.notify(ID, makeNotification(context))
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun makeNotificationChannel(name: String) =
            NotificationChannel(
                    CHANNEL_ID,
                    name,
                    NotificationManager.IMPORTANCE_LOW
            )

    /**
     * Make notification widget.
     *
     * @param context
     */
    private fun makeNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_search_white)
                .setCustomContentView(RemoteViewsFactory()(context))
                .setOngoing(true)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
    }

    /**
     * Hide notification widget.
     * @param context
     */
    fun hide(context: Context) {
        NotificationManagerCompat
                .from(context)
                .cancel(ID)
    }

    /**
     * Refresh.
     * @param context
     */
    fun refresh(context: Context) {
        hide(context)
        show(context)
    }
}
