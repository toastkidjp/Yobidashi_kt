package jp.toastkid.yobidashi

import android.app.Application
import android.os.Build
import android.webkit.WebView
import io.reactivex.disposables.CompositeDisposable
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
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

    /**
     * [CompositeDisposable].
     */
    private val disposables = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }

        val preferenceApplier = PreferenceApplier(this)

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
        DefaultBackgroundImagePreparation()(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        disposables.clear()
    }

}
