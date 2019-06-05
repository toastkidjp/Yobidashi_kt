package jp.toastkid.yobidashi.notification.widget

import android.app.Notification
import android.content.Context
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
     * @param context
     */
    fun show(context: Context) {
        NotificationManagerCompat
                .from(context)
                .notify(ID, makeNotification(context))
    }

    /**
     * Make notification widget.
     *
     * @param context
     */
    private fun makeNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_search_white)
                .setCustomContentView(RemoteViewsFactory.make(context))
                .setOngoing(true)
                .setAutoCancel(false)
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
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(ID)
        manager.notify(ID, makeNotification(context))
    }
}
