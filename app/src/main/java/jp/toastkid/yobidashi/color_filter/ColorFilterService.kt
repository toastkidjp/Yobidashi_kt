package jp.toastkid.yobidashi.color_filter

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.app.JobIntentService
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Overlay Color filter.
 *
 * @author toastkidjp
 */
class ColorFilterService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {

        if (running) {
            handler.post {
                windowManager?.removeView(filterView)
                running = false
            }
            return
        }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        )

        if (filterView == null) {
            filterView = LayoutInflater.from(this).inflate(R.layout.color_filter, null)
            filterView?.setBackgroundColor(PreferenceApplier(applicationContext).filterColor())
        }

        handler.post {
            if (filterView?.parent != null) {
                windowManager?.removeView(filterView)
            }
            windowManager?.addView(filterView, layoutParams)
            running = true
        }
    }

    @Suppress("DEPRECATION")
    private fun getWindowType(): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY

    companion object {

        /** Job ID. */
        private const val JOB_ID = 0x001

        /** Color filter view.  */
        private var filterView: View? = null

        /** Window manager.  */
        private var windowManager: WindowManager? = null

        /** Use for switching view visibility. */
        private val handler = Handler(Looper.getMainLooper())

        /** Flag of running state. */
        private var running: Boolean = false

        /**
         * Draw filter.
         *
         * @param applicationContext Application context.
         */
        internal fun start(applicationContext: Context) {
            running = false
            enqueue(applicationContext)
        }

        /**
         * Remove filter.
         *
         * @param applicationContext Application context.
         */
        internal fun stop(applicationContext: Context) {
            running = true
            enqueue(applicationContext)
        }

        /**
         * Apply new color int.
         */
        internal fun color(@ColorInt color: Int) {
            filterView?.setBackgroundColor(color)
        }

        private fun enqueue(context: Context) {
            enqueueWork(
                    context,
                    ColorFilterService::class.java,
                    JOB_ID,
                    Intent(context, ColorFilterService::class.java)
            )
        }
    }
}
