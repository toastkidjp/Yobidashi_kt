package jp.toastkid.yobidashi.main

import android.content.res.Configuration
import android.os.Build
import java.util.*

/**
 * @author toastkidjp
 */
object LocaleWrapper {

    private val JAPANESE = Locale.JAPAN.language

    fun isJa(configuration: Configuration): Boolean {
        return getLocale(configuration) == JAPANESE
    }

    fun getLocale(configuration: Configuration): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = configuration.locales
            if (locales.isEmpty) {
                return Locale.getDefault().language
            }
            return locales.get(0).language
        }
        return configuration.locale.language
    }

}
