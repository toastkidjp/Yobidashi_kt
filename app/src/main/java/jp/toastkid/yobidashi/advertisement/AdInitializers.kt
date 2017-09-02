package jp.toastkid.yobidashi.advertisement

import android.content.Context

import jp.toastkid.yobidashi.BuildConfig

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
