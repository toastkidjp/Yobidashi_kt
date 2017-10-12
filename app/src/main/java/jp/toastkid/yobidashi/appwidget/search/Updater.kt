package jp.toastkid.yobidashi.appwidget.search

import android.appwidget.AppWidgetManager
import android.content.ContextWrapper
import android.content.Intent

import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.notification.widget.NotificationWidget

/**
 * App-Widget updater.
 *
 * @author toastkidjp
 */
internal object Updater {

    /**
     * Update app-widget intent.
     */
    private val INTENT_UPDATE_WIDGET = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)

    /**
     * Do update app-widget.
     *
     * @param wrapper [ContextWrapper]
     */
    fun update(wrapper: ContextWrapper) {
        Provider.updateWidget(
                wrapper.applicationContext,
                RemoteViewsFactory.make(wrapper)
        )

        if (PreferenceApplier(wrapper).useNotificationWidget()) {
            NotificationWidget.refresh(wrapper)
        }
    }
}
