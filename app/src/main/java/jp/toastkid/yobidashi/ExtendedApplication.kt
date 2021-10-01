package jp.toastkid.yobidashi

import android.app.Application
import android.webkit.WebView
import androidx.core.content.ContextCompat
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.browser.FaviconFolderProviderService
import jp.toastkid.yobidashi.browser.bookmark.BookmarkInitializer
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

        preferenceApplier.color = ContextCompat.getColor(this, R.color.colorPrimaryDark)

        DefaultColorInsertion().insert(this)
        BookmarkInitializer(FaviconFolderProviderService().invoke(this))(this)
        DefaultBackgroundImagePreparation()(this) {
            preferenceApplier.backgroundImagePath = it.absolutePath
        }

        preferenceApplier.setDefaultSearchEngine(SearchCategory.getDefaultCategoryName())
    }

}
