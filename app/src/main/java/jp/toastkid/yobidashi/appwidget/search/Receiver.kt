package jp.toastkid.yobidashi.appwidget.search

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Broadcast receiver.
 *
 * @author toastkidjp
 */
class Receiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            Provider.updateWidget(
                    context.applicationContext,
                    RemoteViewsFactory.make(context)
            )
        }
    }
}