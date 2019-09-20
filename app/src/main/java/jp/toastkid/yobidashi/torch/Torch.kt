package jp.toastkid.yobidashi.torch

import com.journeyapps.barcodescanner.camera.CameraManager
import jp.toastkid.yobidashi.R
import timber.log.Timber

/**
 * Torch API facade.
 *
 * @author toastkidjp
 */
class Torch(
        private val cameraManager: CameraManager,
        private val errorCallback: (Int) -> Unit
) {

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
            errorCallback(R.string.message_fail_to_connect_to_camera)
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