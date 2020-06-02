package jp.toastkid.yobidashi.browser.history

import android.content.Context
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ViewHistoryInsertion private constructor(
        private val context: Context,
        private val title: String,
        private val url: String,
        private val faviconPath: String
) {

    operator fun invoke(): Job =
            if (title.isEmpty() || url.isEmpty()) Job()
            else insert(makeItem(title, url, faviconPath))

    private fun insert(searchHistory: ViewHistory): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            DatabaseFinder().invoke(context)
                    .viewHistoryRepository()
                    .add(searchHistory)
        }
    }

    private fun makeItem(title: String, url: String, faviconPath: String): ViewHistory {
        val sh = ViewHistory()
        sh.title = title
        sh.url = url
        sh.favicon = faviconPath
        sh.lastViewed = System.currentTimeMillis()
        return sh
    }

    companion object {

        fun make(
                context: Context,
                title: String,
                url: String,
                faviconPath: String
        ) = ViewHistoryInsertion(context, title, url, faviconPath)
    }
}