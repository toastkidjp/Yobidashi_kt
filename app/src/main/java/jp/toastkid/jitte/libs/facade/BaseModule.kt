package jp.toastkid.jitte.libs.facade

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * Facade of module.

 * @author toastkidjp
 */
abstract class BaseModule
/**
 * Initialize with parent.
 * @param parent
 */
(
        /**
         * Module's view.
         */
        val moduleView: View) {

    private val mainThreadHandler: Handler

    init {
        mainThreadHandler = Handler(Looper.getMainLooper())
    }

    open fun show() {
        if (moduleView.visibility == View.GONE) {
            mainThreadHandler.post { moduleView.visibility = View.VISIBLE }
        }
    }

    open fun hide() {
        if (moduleView.visibility == View.VISIBLE) {
            mainThreadHandler.post { moduleView.visibility = View.GONE }
        }
    }

    val isVisible: Boolean
        get() = moduleView.visibility == View.VISIBLE

    fun context(): Context {
        return moduleView.context
    }
}
