package jp.toastkid.yobidashi.color_filter

import android.app.Activity
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
 *
 * Initialize with two parent object.
 * @param activity Service's parent activity
 * @param parent   Snackbar's parent
 * @author toastkidjp
 */
class ColorFilter(private val activity: Activity, private val parent: View) {

    /** Snackbar's color.  */
    private val colorPair: ColorPair = PreferenceApplier(activity).colorPair()

    /**
     * Start color filter.
     *
     * @return
     */
    fun start() {
        Toaster.snackShort(
                parent,
                R.string.message_enable_color_filter,
                colorPair
        )
        ColorFilterService.start(activity)
    }

    /**
     * Stop color filter.
     *
     * @return
     */
    fun stop(): Boolean {
        Toaster.snackShort(
                parent,
                R.string.message_stop_color_filter,
                colorPair
        )
        ColorFilterService.stop(activity)
        return true
    }

    /**
     * Switch color filter's state.
     */
    fun switchState(fragment: Fragment, requestCode: Int): Boolean {
        val preferenceApplier = PreferenceApplier(fragment.activity)
        val newState = !preferenceApplier.useColorFilter()
        if (!newState) {
            stop()
            preferenceApplier.setUseColorFilter(newState)
            return newState
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(fragment.activity)) {
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
