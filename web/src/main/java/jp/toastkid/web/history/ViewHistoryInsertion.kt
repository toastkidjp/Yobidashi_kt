package jp.toastkid.web.history

import android.content.Context
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.yobidashi.browser.history.ViewHistory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ViewHistoryInsertion private constructor(
        context: Context,
        private val title: String,
        private val url: String,
        private val faviconPath: String,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val repository = RepositoryFactory().viewHistoryRepository(context)

    operator fun invoke(): Job =
            if (title.isEmpty() || url.isEmpty()) Job()
            else insert(makeItem(title, url, faviconPath))

    private fun insert(searchHistory: ViewHistory): Job =
        CoroutineScope(dispatcher).launch {
            repository.add(searchHistory)
        }

    private fun makeItem(title: String, url: String, faviconPath: String): ViewHistory =
        ViewHistory().also {
            it.title = title
            it.url = url
            it.favicon = faviconPath
            it.lastViewed = System.currentTimeMillis()
        }

    companion object {

        /**
         * Make object with content strings.
         *
         * @param context For obtaining repository
         * @param title title string
         * @param url URL string
         * @param faviconPath Path to favicon
         */
        fun make(
                context: Context,
                title: String,
                url: String,
                faviconPath: String,
                dispatcher: CoroutineDispatcher = Dispatchers.IO
        ) = ViewHistoryInsertion(context, title, url, faviconPath, dispatcher)

    }

}