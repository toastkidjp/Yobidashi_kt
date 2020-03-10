package jp.toastkid.yobidashi.editor

import android.text.TextUtils

/**
 * Converter of quotation style(Markdown).
 *
 * @author toastkidjp
 */
class Quotation {

    /**
     * Line separator.
     */
    private val lineSeparator = System.getProperty("line.separator") ?: "\n"

    /**
     * Invoke quotation function.
     *
     * @param str Nullable [CharSequence]
     */
    operator fun invoke(str: CharSequence?): CharSequence? {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        return str?.split(lineSeparator)
                ?.asSequence()
                ?.map { "> $it" }
                ?.reduce { str1, str2 -> str1 + lineSeparator + str2 }
    }
}