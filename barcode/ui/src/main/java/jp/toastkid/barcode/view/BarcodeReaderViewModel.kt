package jp.toastkid.barcode.view

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import jp.toastkid.barcode.model.BarcodeAnalyzer
import java.util.concurrent.Executors

class BarcodeReaderViewModel {

    private val surfaceRequest = mutableStateOf<SurfaceRequest?>(null)
    private val camera = mutableStateOf<Camera?>(null)
    private val imageCapture = mutableStateOf<ImageCapture?>(null)

    private val result = mutableStateOf("")

    fun surfaceRequest() = surfaceRequest.value

    fun launch(
        lifecycleOwner: LifecycleOwner,
        cameraProvider: ProcessCameraProvider
    ) {
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { req -> surfaceRequest.value = req }
        }

        val img = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        //val executor = ContextCompat.getMainExecutor(context)

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(Executors.newCachedThreadPool(), BarcodeAnalyzer { text ->
                    if (text == result.value) {
                        return@BarcodeAnalyzer
                    }
                    result.value = text
                })
            }

        cameraProvider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // Bind all use cases together so they share the same internal camera session.
        camera.value = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, img, imageAnalysis)
        imageCapture.value = img
    }

    fun existsResult(): Boolean {
        return result.value.isNotBlank()
    }

    fun result() = result.value

}