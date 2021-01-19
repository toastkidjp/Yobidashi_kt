package jp.toastkid.yobidashi

import android.app.Application
import android.webkit.WebView
import androidx.core.content.ContextCompat
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.notification.widget.NotificationWidget
import jp.toastkid.yobidashi.settings.background.DefaultBackgroundImagePreparation
import jp.toastkid.yobidashi.settings.color.DefaultColorInsertion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * For using LeakCanary and so on...
 *
 * @author toastkidjp
 */
class ExtendedApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val preferenceApplier = PreferenceApplier(this)
        preferenceApplier.color = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        CoroutineScope(Dispatchers.Default).launch {
            processForFirstLaunch(preferenceApplier)
        }

        if (preferenceApplier.useNotificationWidget()) {
            NotificationWidget.show(this)
        }
    }

    /**
     * Process for first launch.
     *
     * @param preferenceApplier
     */
    private fun processForFirstLaunch(preferenceApplier: PreferenceApplier) {
        if (!preferenceApplier.isFirstLaunch) {
            return
        }

        DefaultColorInsertion().insert(this)
        preferenceApplier.updateLastAd()
        BookmarkInitializer()(this)
        DefaultBackgroundImagePreparation()(this) {
            preferenceApplier.backgroundImagePath = it.absolutePath
        }
    }

    override fun onTerminate() {
        GlobalWebViewPool.dispose()
        super.onTerminate()
    }

}
