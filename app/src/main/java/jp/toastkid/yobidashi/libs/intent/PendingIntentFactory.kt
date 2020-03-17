package jp.toastkid.yobidashi.libs.intent

import android.app.PendingIntent
import android.content.Context
import androidx.core.net.toUri
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.MainActivityIntentFactory
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.settings.SettingsActivity

/**
 * Factory of [PendingIntent].
 *
 * @author toastkidjp
 */
class PendingIntentFactory {

    /**
     * Make launch search intent.
     *
     * @param context
     * @return [SearchActivity]'s pending intent
     */
    fun makeSearchLauncher(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    0,
                    mainActivityIntentFactory.search(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

    /**
     * Return Launcher Intent of Launcher Activity.
     *
     * @param context
     * @return
     */
    fun launcher(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    6,
                    mainActivityIntentFactory.launcher(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

    /**
     * Make barcode reader intent.
     *
     * @param context
     * @return
     */
    fun barcode(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    8,
                    mainActivityIntentFactory.barcodeReader(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

    /**
     * Make launching browser activity's [PendingIntent].
     *
     * @param context [Context]
     * @return [PendingIntent]
     */
    fun browser(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    9,
                    mainActivityIntentFactory.browser(context, PreferenceApplier(context).homeUrl.toUri()),
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

    /**
     * Make launching bookmark activity's [PendingIntent].
     *
     * @param context [Context]
     * @return [PendingIntent]
     */
    fun bookmark(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    10,
                    mainActivityIntentFactory.bookmark(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

    /**
     * Make launching Random Wikipedia [PendingIntent].
     *
     * @param context [Context]
     * @return [PendingIntent]
     */
    fun randomWikipedia(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    11,
                    mainActivityIntentFactory.randomWikipedia(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

    /**
     * Make Setting activity's [PendingIntent].
     *
     * @param context [Context]
     * @return [PendingIntent]
     */
    fun setting(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    12,
                    SettingsActivity.makeIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

    companion object {
        private val mainActivityIntentFactory = MainActivityIntentFactory()
    }
}
