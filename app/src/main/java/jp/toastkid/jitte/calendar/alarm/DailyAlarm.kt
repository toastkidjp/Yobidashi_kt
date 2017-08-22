package jp.toastkid.jitte.calendar.alarm

import android.app.AlarmManager
import android.content.Context
import android.os.Build

import java.util.Calendar

import jp.toastkid.jitte.libs.intent.PendingIntentFactory

import android.content.Context.ALARM_SERVICE

/**
 * @author toastkidjp
 */
class DailyAlarm(
        /** Context.  */
        private val context: Context) {

    /** Alarm manager.  */
    private val alarmManager: AlarmManager

    init {
        this.alarmManager = this.context.getSystemService(ALARM_SERVICE) as AlarmManager
    }

    private fun set() {

        val triggerAtMillis = calcTriggerAtMillis()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC,
                    triggerAtMillis,
                    AlarmManager.INTERVAL_DAY,
                    PendingIntentFactory.daily(context)
            )
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC,
                    triggerAtMillis,
                    AlarmManager.INTERVAL_DAY,
                    PendingIntentFactory.daily(context)
            )
        }
    }

    private fun calcTriggerAtMillis(): Long {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.HOUR) >= 8) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        calendar.set(Calendar.HOUR, 8)
        calendar.set(Calendar.MINUTE, 0)
        return calendar.timeInMillis
    }

    fun reset() {
        cancel()
        set()
    }

    fun cancel() {
        alarmManager.cancel(PendingIntentFactory.daily(context))
    }
}
