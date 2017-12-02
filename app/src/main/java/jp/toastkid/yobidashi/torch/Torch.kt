package jp.toastkid.yobidashi.torch

import android.content.Context
import com.journeyapps.barcodescanner.camera.CameraManager
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import timber.log.Timber
import java.lang.RuntimeException

/**
 * Torch API facade.
 *
 * @author toastkidjp
 */
class Torch(private val context: Context) {

    /**
     * Use for switching device's torch.
     */
    private val cameraManager = CameraManager(context)

    /**
     * Switch torch state.
     */
    fun switch() {
        try {
            if (!cameraManager.isOpen) {
                cameraManager.open()
            }
            cameraManager.setTorch(!cameraManager.isTorchOn)
            if (!cameraManager.isTorchOn && cameraManager.isOpen) {
                cameraManager.close()
            }
        } catch (e: RuntimeException) {
            Timber.e(e)
            Toaster.tShort(context, context.getString(R.string.message_fail_to_connect_to_camera))
        }
    }

    /**
     * Dispose cameraManager instance.
     */
    fun dispose() {
        if (cameraManager.isOpen) {
            cameraManager.close()
        }
    }
}