package jp.toastkid.jitte

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import jp.toastkid.jitte.browser.bookmark.BookmarkInitializer
import jp.toastkid.jitte.libs.preference.PreferenceApplier
import jp.toastkid.jitte.notification.widget.NotificationWidget
import jp.toastkid.jitte.settings.color.SavedColors
import timber.log.Timber

/**
 * For using LeakCanary and so on...

 * @author toastkidjp
 */
class ExtendedApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LeakCanary.install(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val preferenceApplier = PreferenceApplier(this)

        if (preferenceApplier.isFirstLaunch) {
            SavedColors.insertDefaultColors(this)
            preferenceApplier.updateLastAd()
            BookmarkInitializer.invoke(this)
        }

        BookmarkInitializer.invoke(this)
        if (preferenceApplier.useNotificationWidget()) {
            NotificationWidget.show(this)
        }
    }

}
