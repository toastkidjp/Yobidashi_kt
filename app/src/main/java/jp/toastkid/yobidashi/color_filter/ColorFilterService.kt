package jp.toastkid.yobidashi.color_filter

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager

import jp.toastkid.yobidashi.R

/**
 * Overlay Color filter.

 * @author toastkidjp
 */
class ColorFilterService : Service() {

    /** Color filter view.  */
    private var filterView: View? = null

    /** Window manager.  */
    private var windowManager: WindowManager? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        )
        filterView = LayoutInflater.from(this).inflate(R.layout.color_filter, null)
        windowManager!!.addView(filterView, layoutParams)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager!!.removeView(filterView)
    }
}
