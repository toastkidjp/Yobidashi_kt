package jp.toastkid.yobidashi.appwidget.search

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

/**
 * Web-search type app-widget provider.
 *
 * @author toastkidjp
 */
internal class Provider : AppWidgetProvider() {

    override fun onUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
    ) {
        updateWidget(context, RemoteViewsFactory.make(context))
    }

    companion object {

        /**
         * Update widget.
         *
         * @param context
         * @param remoteViews
         */
        fun updateWidget(context: Context, remoteViews: RemoteViews) {
            val componentName = ComponentName(context, Provider::class.java)
            val manager = AppWidgetManager.getInstance(context)
            manager.updateAppWidget(componentName, remoteViews)
        }
    }
}