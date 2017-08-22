package jp.toastkid.jitte.calendar

import android.content.Context
import android.net.Uri

import jp.toastkid.jitte.R
import jp.toastkid.jitte.libs.intent.CustomTabsFactory
import jp.toastkid.jitte.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class CalendarArticleLinker
/**

 * @param context
 * *
 * @param month
 * *
 * @param dayOfMonth
 */
(
        private val context: Context,
        private val month: Int,
        private val dayOfMonth: Int
) {

    private val applier: PreferenceApplier

    init {
        applier = PreferenceApplier(context)
    }

    operator fun invoke() {
        openCalendarArticle()
    }

    /**
     * Open calendar wikipedia article.
     */
    private fun openCalendarArticle() {
        val url = DateArticleUrlFactory.make(context, month, dayOfMonth)
        if (url.length == 0) {
            return
        }
        CustomTabsFactory
                .make(context, applier.colorPair(), R.drawable.ic_back)
                .build()
                .launchUrl(context, Uri.parse(url))
    }

}
