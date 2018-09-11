package jp.toastkid.yobidashi.editor

import android.text.TextUtils

/**
 * Converter of quotation style(Markdown).
 *
 * @author toastkidjp
 */
object Quotation {

    private val lineSeparator = System.getProperty("line.separator")

    operator fun invoke(str: String?): String? {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        return str?.split(lineSeparator)
                ?.map { "> $it" }
                ?.reduce { str1, str2 -> str1 + lineSeparator + str2 }
    }
}