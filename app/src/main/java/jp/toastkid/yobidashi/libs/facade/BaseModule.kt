package jp.toastkid.yobidashi.libs.facade

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * Facade of module.
 *
 * @param moduleView Module's view.
 * @author toastkidjp
 */
abstract class BaseModule(val moduleView: View) {

    /**
     * For running view operation.
     */
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    var enable: Boolean = true

    /**
     * Show this module.
     */
    open fun show() {
        if (moduleView.visibility == View.GONE && enable) {
            mainThreadHandler.post { moduleView.visibility = View.VISIBLE }
        }
    }

    /**
     * Hide this module.
     */
    open fun hide() {
        if (moduleView.visibility == View.VISIBLE) {
            mainThreadHandler.post { moduleView.visibility = View.GONE }
        }
    }

    /**
     * Is visible this module visible.
     */
    val isVisible: Boolean
        get() = moduleView.visibility == View.VISIBLE

    /**
     * Return view context.
     *
     * @return context
     */
    fun context(): Context = moduleView.context
}
