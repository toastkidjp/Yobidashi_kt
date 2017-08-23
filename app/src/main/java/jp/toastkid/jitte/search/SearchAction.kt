package jp.toastkid.jitte.search

import android.content.Context
import android.os.Bundle
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import jp.toastkid.jitte.analytics.LogSender
import jp.toastkid.jitte.libs.preference.PreferenceApplier
import jp.toastkid.jitte.search.history.SearchHistoryInsertion

/**
 * @author toastkidjp
 */
class SearchAction(
        private val activityContext: Context,
        private val category: String,
        private val query: String
) {

    private val logSender: LogSender

    private val preferenceApplier: PreferenceApplier

    init {
        this.logSender = LogSender(activityContext)
        this.preferenceApplier = PreferenceApplier(activityContext)
    }

    operator fun invoke(): Disposable {

        val disposable = if (preferenceApplier.isEnableSearchHistory) {
            SearchHistoryInsertion.make(activityContext, category, query).insert()
        } else {
            Disposables.empty()
        }

        val bundle = Bundle()
        bundle.putString("category", category)
        bundle.putString("query", query)
        logSender.send("search", bundle)

        val colorPair = preferenceApplier.colorPair()
        if (preferenceApplier.useInternalBrowser()) {
            InternalSearchIntentLauncher(activityContext)
                    .setCategory(category)
                    .setQuery(query)
                    .invoke()
        } else {
            ChromeTabsSearchIntentLauncher(activityContext)
                    .setBackgroundColor(colorPair.bgColor())
                    .setFontColor(colorPair.fontColor())
                    .setCategory(category)
                    .setQuery(query)
                    .invoke()
        }
        return disposable
    }

}
