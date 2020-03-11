package jp.toastkid.yobidashi.libs.intent

import android.app.PendingIntent
import android.content.Context
import androidx.core.net.toUri
import jp.toastkid.yobidashi.barcode.BarcodeReaderActivity
import jp.toastkid.yobidashi.launcher.LauncherActivity
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.MainActivity
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
                    SearchActivity.makeIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

    /**
     * Make launch main activity.
     *
     * @param context
     * @return
     */
    fun main(context: Context): PendingIntent =
            PendingIntent.getActivity(
                    context,
                    3,
                    MainActivity.makeIntent(context),
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
                    LauncherActivity.makeIntent(context),
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
                    BarcodeReaderActivity.makeIntent(context),
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
                    MainActivity.makeBrowserIntent(context, PreferenceApplier(context).homeUrl.toUri()),
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
                    MainActivity.makeBookmarkIntent(context),
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
                    MainActivity.makeRandomWikipediaIntent(context),
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
}
