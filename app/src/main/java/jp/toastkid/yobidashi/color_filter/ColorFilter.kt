package jp.toastkid.yobidashi.color_filter

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.Fragment
import android.view.View

import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Overlay color filter facade.

 * @author toastkidjp
 */
class ColorFilter
/**
 * Initialize with two parent object.

 * @param activity
 *
 * @param parent
 */
(
        /** Service's parent activity.  */
        private val activity: Activity,
        /** Snackbar's parent  */
        private val parent: View) {

    /** Snackbar's color.  */
    private val colorPair: ColorPair

    init {
        this.colorPair = PreferenceApplier(activity).colorPair()
    }

    /**
     * Start color filter.

     * @return
     */
    fun start(): ComponentName {
        Toaster.snackShort(
                parent,
                R.string.message_enable_color_filter,
                colorPair
        )
        return activity.startService(Intent(activity, ColorFilterService::class.java))
    }

    /**
     * Stop color filter.

     * @return
     */
    fun stop(): Boolean {
        Toaster.snackShort(
                parent,
                R.string.message_stop_color_filter,
                colorPair
        )
        return activity.stopService(Intent(activity, ColorFilterService::class.java))
    }

    fun switchState(fragment: Fragment, requestCode: Int): Boolean {
        val preferenceApplier = PreferenceApplier(fragment.activity)
        val newState = !preferenceApplier.useColorFilter()
        if (!newState) {
            stop()
            preferenceApplier.setUseColorFilter(newState)
            return newState
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(fragment.activity)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + fragment.activity.packageName))
            fragment.startActivityForResult(intent, requestCode)
            return !newState
        }
        start()
        preferenceApplier.setUseColorFilter(newState)
        return newState
    }
}
