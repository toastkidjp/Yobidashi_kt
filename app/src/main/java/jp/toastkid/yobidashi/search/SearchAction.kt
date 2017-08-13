package jp.toastkid.yobidashi.search

import android.content.Context
import android.os.Bundle

import jp.toastkid.yobidashi.analytics.LogSender
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

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

    operator fun invoke() {
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
    }
}
