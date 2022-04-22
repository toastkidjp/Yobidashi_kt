package jp.toastkid.yobidashi

import android.app.Application
import android.webkit.WebView
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.initial.FirstLaunchInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * For using LeakCanary and so on...
 *
 * @author toastkidjp
 */
@Suppress("unused")
class ExtendedApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val preferenceApplier = PreferenceApplier(this)

        CoroutineScope(Dispatchers.Default).launch {
            FirstLaunchInitializer(this@ExtendedApplication, preferenceApplier)
        }
    }

}
