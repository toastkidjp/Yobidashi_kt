package jp.toastkid.jitte.notification.widget

import android.app.Notification
import android.content.Context
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat

import jp.toastkid.jitte.R

/**
 * Notification widget's launcher.

 * @author toastkidjp
 */
object NotificationWidget {

    /** Notification ID.  */
    private val ID = 1

    /**
     * Show notification widget.
     * @param context
     */
    fun show(context: Context) {
        val notification = makeNotification(context)
        NotificationManagerCompat.from(context).notify(ID, notification)
    }

    private fun makeNotification(context: Context): Notification {
        return NotificationCompat.Builder(context)
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
        NotificationManagerCompat.from(context).cancel(ID)
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
