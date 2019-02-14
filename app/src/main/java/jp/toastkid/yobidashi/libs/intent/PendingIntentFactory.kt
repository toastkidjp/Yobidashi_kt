package jp.toastkid.yobidashi.libs.intent

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import jp.toastkid.yobidashi.barcode.BarcodeReaderActivity
import jp.toastkid.yobidashi.launcher.LauncherActivity
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.search.SearchActivity
import jp.toastkid.yobidashi.search.favorite.AddingFavoriteSearchService

/**
 * Factory of [PendingIntent].
 *
 * @author toastkidjp
 */
object PendingIntentFactory {

    /**
     * Make launch search intent.
     * @param context
     *
     * @return [SearchActivity]'s pending intent
     */
    fun makeSearchLauncher(context: Context): PendingIntent {
        val intent = SearchActivity.makeIntent(context)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Make launch main activity.
     * @param context
     *
     * @return
     */
    fun main(context: Context): PendingIntent {
        return PendingIntent.getActivity(
                context,
                3,
                MainActivity.makeIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Make calling calendar intent.
     * @param context
     *
     * @param month
     *
     * @param dayOfMonth
     *
     * @return
     */
    fun calendar(
            context: Context, month: Int, dayOfMonth: Int): PendingIntent {
        return PendingIntent.getActivity(
                context,
                5,
                MainActivity.makeIntent(context, month, dayOfMonth),
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Return Launcher Intent of Launcher Activity.

     * @param context
     *
     * @return
     */
    fun launcher(context: Context): PendingIntent {
        return PendingIntent.getActivity(
                context,
                6,
                LauncherActivity.makeIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Make adding favorite search intent.
     * @param context
     *
     * @param category
     *
     * @param query
     *
     * @return [AddingFavoriteSearchService]'s pending intent
     */
    fun favoriteSearchAdding(
            context: Context,
            category: String,
            query: String
    ): PendingIntent {
        val intent = Intent(context, AddingFavoriteSearchService::class.java)
        intent.putExtra(AddingFavoriteSearchService.EXTRA_KEY_CATEGORY, category)
        intent.putExtra(AddingFavoriteSearchService.EXTRA_KEY_QUERY, query)
        return PendingIntent.getService(context, 7, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Make barcode reader intent.
     * @param context
     *
     * @return
     */
    fun barcode(context: Context): PendingIntent {
        val intent = BarcodeReaderActivity.makeIntent(context)
        return PendingIntent.getActivity(context, 8, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
