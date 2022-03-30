/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.barcode

import android.Manifest
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.barcode.model.BarcodeAnalyzer
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.interop.ComposeViewFactory
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.viewmodel.WebSearchViewModel

/**
 * Barcode reader function fragment.
 *
 * @author toastkidjp
 */
class BarcodeReaderFragment : Fragment() {

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    private val cameraPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                onResume.value = true
                return@registerForActivityResult
            }

            contentViewModel?.snackShort(R.string.message_requires_permission_camera)
            parentFragmentManager.popBackStack()
        }

    /**
     * Required permission for this fragment(and function).
     */
    private val cameraPermission = Manifest.permission.CAMERA

    private val onResume = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = context ?: return null

        if (isNotGranted()) {
            cameraPermissionRequestLauncher.launch(cameraPermission)
        }

        preferenceApplier = PreferenceApplier(context)

        contentViewModel = activity?.let { ViewModelProvider(it).get(ContentViewModel::class.java) }

        return ComposeViewFactory().invoke(context) {
            val lifecycleOwner = LocalLifecycleOwner.current
            val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
            val onResume = remember { onResume }
            val result = remember { mutableStateOf("") }

            if (onResume.value.not()) {
                return@invoke
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    val previewView = PreviewView(context)
                    val executor = ContextCompat.getMainExecutor(context)
                    cameraProviderFuture.addListener(
                        {
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build()

                            val cameraProvider = cameraProviderFuture.get()
                            cameraProvider.unbindAll()

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(executor, BarcodeAnalyzer { newResult ->
                                        val text = newResult.text
                                        println("tomato text $text")
                                        if (text == result.value) {
                                            return@BarcodeAnalyzer
                                        }
                                        result.value = text
                                    })
                                }

                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                imageAnalysis,
                                preview
                            )
                        },
                        executor
                    )
                    previewView
                }
            )

            if (result.value.isNotBlank()) {
                MaterialTheme() {
                    Surface(
                        elevation = 4.dp,
                        modifier = Modifier.background(Color(preferenceApplier.color))
                    ) {
                        Column() {
                            Row() {
                                Text(
                                    stringResource(id = R.string.clip),
                                    color = Color(preferenceApplier.fontColor),
                                    modifier = Modifier.clickable { clip(result.value) }
                                )
                                Text(
                                    stringResource(id = R.string.share),
                                    color = Color(preferenceApplier.fontColor),
                                    modifier = Modifier.clickable {
                                        startActivity(ShareIntentFactory()(result.value))
                                    }
                                )
                                Text(
                                    stringResource(id = R.string.open),
                                    color = Color(preferenceApplier.fontColor),
                                    modifier = Modifier.clickable {
                                        val activity = activity ?: return@clickable
                                        ViewModelProvider(activity)
                                            .get(WebSearchViewModel::class.java)
                                            .search(result.value)
                                    }
                                )
                            }
                            Text(result.value)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (isNotGranted()) {
            cameraPermissionRequestLauncher.launch(cameraPermission)
            return
        }

        onResume.value = true
    }

    override fun onPause() {
        super.onPause()
        onResume.value = false
    }

    /**
     * Return is granted required permission.
     *
     * @return If is granted camera permission, return true
     */
    private fun isNotGranted() =
        activity?.checkSelfPermission(cameraPermission) != PackageManager.PERMISSION_GRANTED

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.camera, menu)
    }

    /**
     * Start decode.
     */
    private fun startDecode() {
        /*TODO
        val text = barcodeResult.text
                if (text == result.value) {
                    return
                }
                result.value = text
         */
    }

    /**
     * Copy result text to clipboard.
     *
     * @param text Result text
     */
    private fun clip(text: String) {
        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?)
            ?.setPrimaryClip(
                ClipData(
                    ClipDescription("text_data", arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)),
                    ClipData.Item(text)
                )
            )
    }

    override fun onDetach() {
        cameraPermissionRequestLauncher.unregister()
        super.onDetach()
    }

}