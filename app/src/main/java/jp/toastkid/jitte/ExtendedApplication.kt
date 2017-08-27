package jp.toastkid.jitte

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import jp.toastkid.jitte.libs.preference.PreferenceApplier
import jp.toastkid.jitte.notification.widget.NotificationWidget
import jp.toastkid.jitte.settings.color.SavedColors

/**
 * For using LeakCanary and so on...

 * @author toastkidjp
 */
class ExtendedApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LeakCanary.install(this)

        val preferenceApplier = PreferenceApplier(this)

        if (preferenceApplier.isFirstLaunch) {
            SavedColors.insertDefaultColors(this)
            preferenceApplier.updateLastAd()
        }

        if (preferenceApplier.useNotificationWidget()) {
            NotificationWidget.show(this)
        }
    }

}
