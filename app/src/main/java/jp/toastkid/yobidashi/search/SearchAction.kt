package jp.toastkid.yobidashi.search

import android.content.Context
import android.os.Bundle
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.history.SearchHistoryInsertion

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

        logSender.send(
                "search",
                Bundle().apply {
                    putString("category", category)
                    putString("query", query)
                }
        )

        val colorPair: ColorPair = preferenceApplier.colorPair()
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
