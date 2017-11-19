package jp.toastkid.yobidashi.search.favorite

import android.app.SearchManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**

 * @author toastkidjp
 */
class AddingFavoriteSearchService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return Service.START_STICKY_COMPATIBILITY
        }
        FavoriteSearchInsertion(
                applicationContext,
                intent.getStringExtra(EXTRA_KEY_CATEGORY),
                intent.getStringExtra(EXTRA_KEY_QUERY)
        ).invoke()
        return Service.START_STICKY_COMPATIBILITY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {

        val EXTRA_KEY_CATEGORY = "Category"

        val EXTRA_KEY_QUERY = SearchManager.QUERY
    }

}