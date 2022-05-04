package jp.toastkid.yobidashi.appwidget.search

import android.appwidget.AppWidgetManager
import android.content.Context
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.notification.widget.NotificationWidget

/**
 * App-Widget updater.
 *
 * @author toastkidjp
 */
internal class Updater {

    /**
     * Do update app-widget.
     *
     * @param wrapper [Context]
     */
    fun update(wrapper: Context) {
        Provider.updateWidget(
                wrapper.applicationContext,
                AppWidgetManager.getInstance(wrapper),
                RemoteViewsFactory().make(wrapper)
        )

        if (PreferenceApplier(wrapper).useNotificationWidget()) {
            NotificationWidget.refresh(wrapper)
        }
    }
}
