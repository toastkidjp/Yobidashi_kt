package jp.toastkid.yobidashi.appwidget.search

import android.appwidget.AppWidgetManager
import android.content.ContextWrapper
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.notification.widget.NotificationWidget

/**
 * App-Widget updater.
 *
 * @author toastkidjp
 */
internal object Updater {

    /**
     * Do update app-widget.
     *
     * @param wrapper [ContextWrapper]
     */
    fun update(wrapper: ContextWrapper) {
        Provider.updateWidget(
                wrapper.applicationContext,
                AppWidgetManager.getInstance(wrapper),
                RemoteViewsFactory.make(wrapper)
        )

        if (PreferenceApplier(wrapper).useNotificationWidget()) {
            NotificationWidget.refresh(wrapper)
        }
    }
}
