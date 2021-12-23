package jp.toastkid.yobidashi.libs.intent

import android.app.PendingIntent
import android.content.Context
import androidx.core.net.toUri
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.launch.MainActivityIntentFactory
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia

/**
 * Factory of [PendingIntent].
 *
 * @author toastkidjp
 */
class PendingIntentFactory(
        private val mainActivityIntentFactory: MainActivityIntentFactory = MainActivityIntentFactory()
) {

    /**
     * Make launch search intent.
     *
     * @param context
     * @return [MainActivity]'s pending intent with search action
     */
    fun makeSearchLauncher(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    0,
                    mainActivityIntentFactory.search(context),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                    RandomWikipedia.makeIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

    /**
     * Make Setting fragment's [PendingIntent].
     *
     * @param context [Context]
     * @return [PendingIntent]
     */
    fun setting(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    12,
                    mainActivityIntentFactory.setting(context),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

}
