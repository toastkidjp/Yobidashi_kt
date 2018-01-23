package jp.toastkid.yobidashi.calendar

import android.content.Context
import android.net.Uri
import jp.toastkid.yobidashi.libs.intent.CustomTabsFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Calendar article linker.
 *
 * @param context
 * @param month
 * @param dayOfMonth
 *
 * @author toastkidjp
 */
class CalendarArticleLinker(
        private val context: Context,
        private val month: Int,
        private val dayOfMonth: Int
) {

    /**
     * Preferences wrapper.
     */
    private val applier: PreferenceApplier = PreferenceApplier(context)

    /**
     * Open calendar wikipedia article.
     */
    operator fun invoke() {
        val url = DateArticleUrlFactory.make(context, month, dayOfMonth)
        if (url.isEmpty()) {
            return
        }
        CustomTabsFactory
                .make(context, applier.colorPair())
                .build()
                .launchUrl(context, Uri.parse(url))
    }

}
