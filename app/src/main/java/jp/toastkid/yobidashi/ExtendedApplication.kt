package jp.toastkid.yobidashi

import android.app.Application
import android.os.Build
import android.webkit.WebView
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.notification.widget.NotificationWidget
import jp.toastkid.yobidashi.settings.background.DefaultBackgroundImagePreparation
import jp.toastkid.yobidashi.settings.color.DefaultColorInsertion
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

        Completable.fromAction { processForFirstLaunch(preferenceApplier) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { },
                        { Timber.e(it) }
                ).addTo(disposables)

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
        BookmarkInitializer()(this).addTo(disposables)
        DefaultBackgroundImagePreparation()(this).addTo(disposables)
    }

    override fun onTerminate() {
        super.onTerminate()
        disposables.clear()
    }

}
