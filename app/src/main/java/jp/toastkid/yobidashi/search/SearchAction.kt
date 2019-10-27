package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri
import android.os.Bundle
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.search.history.SearchHistoryInsertion
import jp.toastkid.yobidashi.tab.BackgroundTabQueue

/**
 * Search action shortcut.
 *
 * @param activityContext Activity's context
 * @param category Search Category
 * @param query Search query (or URL)
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
     * Log sender.
     */
    private val logSender: LogSender = LogSender(activityContext)

    /**
     * Preferences wrapper.
     */
    private val preferenceApplier: PreferenceApplier = PreferenceApplier(activityContext)

    /**
     * Invoke action.
     */
    operator fun invoke(): Disposable {

        val disposable = if (preferenceApplier.isEnableSearchHistory && isNotUrl(query) && saveHistory) {
            SearchHistoryInsertion.make(activityContext, category, query).insert()
        } else {
            Disposables.empty()
        }

        logSender.send(
                "search",
                Bundle().apply {
                    putString("category", category)
                    putString("query", query)
                }
        )

        val validUrl = Urls.isValidUrl(query)

        if (preferenceApplier.useInternalBrowser()) {
            withInternalBrowser(validUrl)
        } else {
            withChromeTabs(validUrl)
        }
        return disposable
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
     * @param validUrl passed query is URL.
     */
    private fun withInternalBrowser(validUrl: Boolean) {
        if (validUrl) {
            activityContext.startActivity(
                    MainActivity.makeBrowserIntent(activityContext, Uri.parse(query)))
            return
        }
        if (onBackground) {
            BackgroundTabQueue.add(
                    activityContext.getString(R.string.title_tab_background_search, query),
                    UrlFactory.make(activityContext, category, query, currentUrl)
            )
            return
        }

        InternalSearchIntentLauncher(activityContext)
                .setCategory(category)
                .setQuery(query)
                .setCurrentUrl(currentUrl)
                .invoke()
    }

    /**
     * Action with custom chrome tabs.
     *
     * @param validUrl passed query is URL.
     */
    private fun withChromeTabs(validUrl: Boolean) {
        val colorPair: ColorPair = preferenceApplier.colorPair()

        if (validUrl) {
            CustomTabsFactory.make(activityContext, colorPair)
                    .build()
                    .launchUrl(activityContext, Uri.parse(query))
            return
        }
        ChromeTabsSearchIntentLauncher(activityContext)
                .setBackgroundColor(colorPair.bgColor())
                .setFontColor(colorPair.fontColor())
                .setCategory(category)
                .setQuery(query)
                .invoke()
    }

}
