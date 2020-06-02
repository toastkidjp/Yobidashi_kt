package jp.toastkid.yobidashi.browser.bookmark

import android.content.Context
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class BookmarkInsertion (
        private val context: Context,
        private val title: String,
        private val url: String = "",
        private val faviconPath: String = "",
        private val parent: String = "",
        private val folder: Boolean = false
) {

    fun insert(): Job {
        if (title.isEmpty()) {
            return Job()
        }
        return insert(Bookmark.make(title, url, faviconPath, parent, folder))
    }

    private fun insert(bookmark: Bookmark): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            DatabaseFinder().invoke(context).bookmarkRepository()
                    .add(bookmark)
        }
    }

}