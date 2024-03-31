package jp.toastkid.search

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.history.SearchHistoryInsertion
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
 * @param viewModelSupplier
 * @param preferenceApplierSupplier
 * @param urlFactory
 * @author toastkidjp
 */
class SearchAction(
        private val activityContext: Context,
        private val category: String,
        private val query: String,
        private val currentUrl: String? = null,
        private val onBackground: Boolean = false,
        private val saveHistory: Boolean = true,
        private val viewModelSupplier: (Context) -> ContentViewModel? = {
            (activityContext as? ViewModelStoreOwner)?.let {
                ViewModelProvider(it).get(ContentViewModel::class.java)
            }
        },
        private val preferenceApplierSupplier: (Context) -> PreferenceApplier = {
            PreferenceApplier(activityContext)
        },
        private val urlFactory: UrlFactory = UrlFactory()
) {

    /**
     * Invoke action.
     *
     * @return job which invoked search-history insertion
     */
    operator fun invoke(): Job {
        val disposable = insertToSearchHistory()

        val validatedUrl = Urls.isValidUrl(query)

        openUrl(validatedUrl)
        return disposable
    }

    private fun insertToSearchHistory(): Job =
            if (preferenceApplierSupplier(activityContext).isEnableSearchHistory
                && isNotUrl(query) && saveHistory) {
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
    private fun openUrl(validatedUrl: Boolean) {
        val uri =
            if (validatedUrl) { Uri.parse(query) } else { urlFactory(category, query, currentUrl) }
        openUri(uri)
    }

    private fun openUri(uri: Uri) {
        if (onBackground)
            viewModelSupplier(activityContext)?.openBackground(
                    activityContext.getString(R.string.title_tab_background_search, query),
                    uri
            )
        else
            viewModelSupplier(activityContext)?.open(uri)
    }

}
