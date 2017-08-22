package jp.toastkid.jitte.calendar.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * For using reset daily alarm.
 * @author toastkidjp
 */
class TimeChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        DailyAlarm(context).reset()
    }
}
