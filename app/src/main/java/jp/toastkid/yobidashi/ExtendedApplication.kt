package jp.toastkid.yobidashi

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
import jp.toastkid.yobidashi.libs.db.DbInitter
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.notification.widget.NotificationWidget
import jp.toastkid.yobidashi.settings.color.SavedColors
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
        Completable.fromAction { LeakCanary.install(this) }
                .subscribeOn(Schedulers.computation())
                .subscribe({}, {Timber.e(it)})
                .addTo(disposables)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val preferenceApplier = PreferenceApplier(this)

        Completable.fromAction { DbInitter.init(this) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { processForFirstLaunch(preferenceApplier) },
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

        SavedColors.insertDefaultColors(this)
        preferenceApplier.updateLastAd()
        BookmarkInitializer.invoke(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        disposables.clear()
    }

}
