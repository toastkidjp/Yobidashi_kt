package jp.toastkid.yobidashi.torch

import android.content.Context
import com.journeyapps.barcodescanner.camera.CameraManager

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
        if (!cameraManager.isOpen) {
            cameraManager.open()
        }
        cameraManager.setTorch(!cameraManager.isTorchOn)
        if (!cameraManager.isTorchOn && cameraManager.isOpen) {
            cameraManager.close()
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