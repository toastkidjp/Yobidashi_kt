package jp.toastkid.yobidashi.appwidget.search

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @author toastkidjp
 */

class Receiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "UPDATE_WIDGET") {
            Provider.updateWidget(
                    context.applicationContext,
                    RemoteViewsFactory.make(context)
            )
        }
    }
}