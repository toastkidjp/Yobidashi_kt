package jp.toastkid.jitte.appwidget.search

import android.content.ContextWrapper
import android.content.Intent

import jp.toastkid.jitte.libs.preference.PreferenceApplier
import jp.toastkid.jitte.notification.widget.NotificationWidget

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
