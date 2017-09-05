package jp.toastkid.yobidashi.calendar.alarm

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat

import java.util.Calendar

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.calendar.DateTitleFactory
import jp.toastkid.yobidashi.libs.intent.PendingIntentFactory

/**
 * @author toastkidjp
 */
class DailyCalendarReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
    }

    companion object {

        /**
         * Invole notification launching.
         * @param context
         */
        fun showNotification(context: Context) {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(1, makeNotification(context))
        }

        /**
         * Make notification.
         * @param context
         *
         * @return
         */
        private fun makeNotification(context: Context): Notification {
            val cal = Calendar.getInstance()
            val month = cal.get(Calendar.MONTH)
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)

            val title = DateTitleFactory.makeDateTitle(context, month, dayOfMonth)

            return NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_calendar)
                    .setContentTitle(title)
                    .setSubText(context.getString(R.string.subtext_notification))
                    .setWhen(cal.timeInMillis)
                    .setTicker(title)
                    .setContentIntent(PendingIntentFactory.calendar(context, month, dayOfMonth))
                    .build()
        }
    }
}
