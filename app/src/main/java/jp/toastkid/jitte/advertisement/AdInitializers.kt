package jp.toastkid.jitte.advertisement

import android.content.Context

import jp.toastkid.jitte.BuildConfig

/**
 * @author toastkidjp
 */
object AdInitializers {

    fun find(context: Context): AdInitializer {
        if (BuildConfig.DEBUG) {
            return TestAdInitializer(context)
        }
        return ProductionAdInitializer(context)
    }
}
