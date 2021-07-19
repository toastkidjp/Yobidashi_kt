package jp.toastkid.yobidashi.main

import android.content.res.Configuration
import java.util.Locale

/**
 * @author toastkidjp
 */
class LocaleWrapper {

    fun isJa(configuration: Configuration): Boolean {
        return getLocale(configuration) == Locale.JAPAN.language
    }

    private fun getLocale(configuration: Configuration): String {
        val locales = configuration.locales
        return if (locales.isEmpty) Locale.getDefault().language else locales.get(0).language
    }

}
