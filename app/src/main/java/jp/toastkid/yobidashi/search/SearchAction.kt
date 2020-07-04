package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.history.SearchHistoryInsertion
import kotlinx.coroutines.Job

/**
 * Search action shortcut.
 *
 * @param activityContext Activity's context
 * @param category Search Category
 * @param query Search query (or URL)
 * @param currentUrl Current URL for site-search
 * @param onBackground
 * @param saveHistory
 * @author toastkidjp
 */
class SearchAction(
        private val activityContext: Context,
        private val category: String,
        private val query: String,
        private val currentUrl: String? = null,
        private val onBackground: Boolean = false,
        private val saveHistory: Boolean = true
) {

    /**
     * Preferences wrapper.
     */
    private val preferenceApplier: PreferenceApplier = PreferenceApplier(activityContext)

    private val urlFactory = UrlFactory()

    /**
     * Invoke action.
     */
    operator fun invoke(): Job {
        val disposable = insertToSearchHistory()

        val validatedUrl = Urls.isValidUrl(query)

        withInternalBrowser(validatedUrl)
        return disposable
    }

    private fun insertToSearchHistory(): Job =
            if (preferenceApplier.isEnableSearchHistory && isNotUrl(query) && saveHistory) {
                SearchHistoryInsertion.make(activityContext, category, query).insert()
            } else {
                Job()
            }

    /**
     * Check passed query is not URL.
     *
     * @param query
     */
    private fun isNotUrl(query: String): Boolean = !query.contains("://")

    /**
     * Action with internal browser.
     *
     * @param validatedUrl passed query is URL.
     */
    private fun withInternalBrowser(validatedUrl: Boolean) {
        val browserViewModel = (activityContext as? FragmentActivity)?.let {
            ViewModelProvider(it).get(BrowserViewModel::class.java)
        }

        if (validatedUrl) {
            browserViewModel?.open(Uri.parse(query))
            return
        }

        val searchUri = urlFactory(activityContext, category, query, currentUrl)

        if (onBackground) {
            browserViewModel?.openBackground(
                    activityContext.getString(R.string.title_tab_background_search, query),
                    searchUri
            )
            return
        }
        browserViewModel?.open(searchUri)
    }

}
