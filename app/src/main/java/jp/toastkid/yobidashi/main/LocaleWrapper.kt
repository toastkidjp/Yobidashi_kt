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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return configuration.locale.language
        }

        val locales = configuration.locales
        return if (locales.isEmpty) Locale.getDefault().language else locales.get(0).language
    }

}
