package jp.toastkid.yobidashi.libs

import android.app.ActivityOptions
import android.view.View

/**
 * Factory of ActivityOptions.
 *
 * @author toastkidjp
 */
class ActivityOptionsFactory {

    /**
     * Make ActivityOptions by view.
     * @param view
     */
    fun makeScaleUpBundle(view: View): ActivityOptions =
            ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.width, view.height)
}