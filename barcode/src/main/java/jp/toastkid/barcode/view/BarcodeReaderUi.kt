/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.barcode.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import jp.toastkid.barcode.R
import jp.toastkid.barcode.model.BarcodeAnalyzer
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.lib.preference.PreferenceApplier

@Composable
fun BarcodeReaderUi() {
    val context = LocalContext.current as? Activity ?: return

    val preferenceApplier = PreferenceApplier(context)

    val onResume = remember { mutableStateOf(isGranted(context)) }

    val cameraPermissionRequestLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onResume.value = true
                return@rememberLauncherForActivityResult
            }

            (context as? ViewModelStoreOwner)
                ?.let { ViewModelProvider(it).get(ContentViewModel::class.java) }
                ?.snackShort(R.string.message_requires_permission_camera)
        }


    if (isGranted(context).not()) {
        SideEffect {
            cameraPermissionRequestLauncher.launch(cameraPermission)
        }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val result = remember { mutableStateOf("") }

    if (onResume.value.not()) {
        return
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                elevation = 4.dp,
                modifier = Modifier.wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colors.primary)
                        .wrapContentWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(id = R.string.clip),
                            color = MaterialTheme.colors.onPrimary,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable { clip(context, result.value) }
                        )
                        Text(
                            stringResource(id = R.string.share),
                            color = Color(preferenceApplier.fontColor),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    context.startActivity(ShareIntentFactory()(result.value))
                                }
                        )
                        Text(
                            stringResource(id = R.string.open),
                            color = Color(preferenceApplier.fontColor),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    val activity = (context as? ViewModelStoreOwner) ?: return@clickable
                                    ViewModelProvider(activity)
                                        .get(BrowserViewModel::class.java)
                                        .search(result.value)
                                }
                        )
                    }
                    Text(
                        result.value,
                        color = Color(preferenceApplier.fontColor),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Return is granted required permission.
 *
 * @return If is granted camera permission, return true
 */
private fun isGranted(activity: Activity?) =
    activity?.checkSelfPermission(cameraPermission) == PackageManager.PERMISSION_GRANTED

/**
 * Copy result text to clipboard.
 *
 * @param text Result text
 */
private fun clip(context: Context?, text: String) {
    Clipboard.clip(context, text)
}

/**
 * Required permission for this fragment(and function).
 */
private const val cameraPermission = Manifest.permission.CAMERA
