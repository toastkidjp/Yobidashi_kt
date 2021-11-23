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
        updateWidget(context, appWidgetManager, RemoteViewsFactory().make(context))
    }

    companion object {

        /**
         * Update widget.
         *
         * @param context
         * @param appWidgetManager [AppWidgetManager]
         * @param remoteViews
         */
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            remoteViews: RemoteViews
        ) {
            val componentName = ComponentName(context, Provider::class.java)
            appWidgetManager.updateAppWidget(componentName, remoteViews)
        }
    }
}