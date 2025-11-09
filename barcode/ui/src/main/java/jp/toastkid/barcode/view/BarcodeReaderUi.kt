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
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import jp.toastkid.barcode.ui.R
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.intent.ShareIntentFactory

@Composable
fun BarcodeReaderUi() {
    val context = LocalContext.current as? Activity ?: return
    val viewModel = remember { BarcodeReaderViewModel() }

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

    if (onResume.value.not()) {
        return
    }

    viewModel.surfaceRequest()?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = Modifier.fillMaxSize()
        )
    }

    LaunchedEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()
        viewModel.launch(lifecycleOwner, cameraProvider)
    }

    if (viewModel.existsResult()) {
        Result(viewModel.result())
    }
}

@Composable
private fun Result(result: String) {
    val context = LocalContext.current as? Activity ?: return
    val backgroundColor = MaterialTheme.colorScheme.primary
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            shadowElevation = 4.dp,
            modifier = Modifier.wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .drawBehind { drawRect(backgroundColor) }
                    .wrapContentWidth()
                    .padding(16.dp)
            ) {
                Text(
                    result,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            clip(context, result)
                        }
                    ) {
                        Text(
                            stringResource(id = jp.toastkid.lib.R.string.copy),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 16.sp
                        )
                    }
                    Button(
                            onClick = {
                                context.startActivity(ShareIntentFactory()(result))
                            }
                            ) {
                        Text(
                            stringResource(id = jp.toastkid.lib.R.string.share),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 16.sp
                        )
                    }
                    Button(
                        onClick = {
                            val activity = (context as? ViewModelStoreOwner) ?: return@Button
                            ViewModelProvider(activity)
                                .get(ContentViewModel::class.java)
                                .search(result)
                        }
                    ) {
                        Text(
                            stringResource(id = jp.toastkid.lib.R.string.open),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 16.sp
                        )
                    }
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
