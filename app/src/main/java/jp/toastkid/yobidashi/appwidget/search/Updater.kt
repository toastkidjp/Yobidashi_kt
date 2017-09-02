package jp.toastkid.yobidashi.appwidget.search

import android.content.ContextWrapper
import android.content.Intent

import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.notification.widget.NotificationWidget

/**
 * App-Widget updater.

 * @author toastkidjp
 */
object Updater {

    /** Update app-widget intent.  */
    private val INTENT_UPDATE_WIDGET = Intent("UPDATE_WIDGET")

    /**
     * Do update app-widget.

     * @param wrapper [ContextWrapper]
     */
    fun update(wrapper: ContextWrapper) {
        wrapper.sendBroadcast(INTENT_UPDATE_WIDGET)

        if (PreferenceApplier(wrapper).useNotificationWidget()) {
            NotificationWidget.refresh(wrapper)
        }
    }
}
