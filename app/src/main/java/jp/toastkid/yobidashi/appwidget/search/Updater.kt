package jp.toastkid.yobidashi.appwidget.search

import android.content.ContextWrapper
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
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
                RemoteViewsFactory.make(wrapper)
        )

        if (PreferenceApplier(wrapper).useNotificationWidget()) {
            NotificationWidget.refresh(wrapper)
        }
    }
}
