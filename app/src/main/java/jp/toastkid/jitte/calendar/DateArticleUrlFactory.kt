package jp.toastkid.jitte.calendar

import android.content.Context

import java.text.MessageFormat

import jp.toastkid.jitte.R
import jp.toastkid.jitte.main.LocaleWrapper

/**
 * Factory of Wikipedia article's url.

 * @author toastkidjp
 */
internal object DateArticleUrlFactory {

    /** Format resource ID.  */
    private val FORMAT_ID = R.string.format_date_link

    /**
     * Make Wikipedia article's url.
     * @param context context
     * *
     * @param month 0-11
     * *
     * @param dayOfMonth 1-31
     * *
     * @return Wikipedia article's url
     */
    fun make(context: Context, month: Int, dayOfMonth: Int): String {
        if (month < 0 || month >= 12) {
            return ""
        }
        if (dayOfMonth <= 0 || dayOfMonth >= 31) {
            return ""
        }
        if (LocaleWrapper.isJa(context.resources.configuration)) {
            return MessageFormat.format(context.getString(FORMAT_ID), month + 1, dayOfMonth)
        }
        return MessageFormat.format(context.getString(FORMAT_ID), Month.get(month), dayOfMonth)
    }
}
