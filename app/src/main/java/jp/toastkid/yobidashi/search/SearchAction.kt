package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.MainActivityIntentFactory
import jp.toastkid.yobidashi.search.history.SearchHistoryInsertion

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

    private val mainActivityIntentFactory = MainActivityIntentFactory()

    /**
     * Invoke action.
     */
    operator fun invoke(): Disposable {
        val disposable = insertToSearchHistory()

        val validatedUrl = Urls.isValidUrl(query)

        withInternalBrowser(validatedUrl)
        return disposable
    }

    private fun insertToSearchHistory(): Disposable =
            if (preferenceApplier.isEnableSearchHistory && isNotUrl(query) && saveHistory) {
                SearchHistoryInsertion.make(activityContext, category, query).insert()
            } else {
                Disposables.disposed()
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
        if (validatedUrl) {
            // TODO Use ViewModel.
            activityContext.startActivity(
                    mainActivityIntentFactory.browser(activityContext, Uri.parse(query)))
            return
        }

        val searchUri = urlFactory(activityContext, category, query, currentUrl)

        val browserViewModel = (activityContext as? FragmentActivity)?.let {
            ViewModelProviders.of(it).get(BrowserViewModel::class.java)
        }

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
