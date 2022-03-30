/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.barcode.model

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import timber.log.Timber

internal class BarcodeAnalyzer(
    private val reader: MultiFormatReader = MultiFormatReader().also {
        it.setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                    BarcodeFormat.QR_CODE
                )
            )
        )
    },
    private val callback: (Result) -> Unit
) : ImageAnalysis.Analyzer {

    private val acceptableFormats =
        mutableListOf(ImageFormat.YUV_420_888, ImageFormat.YUV_422_888, ImageFormat.YUV_444_888)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image

        if (acceptableFormats.contains(imageProxy.format).not()) {
            imageProxy.close()
            return
        }

        val data = extractArray(image)

        if (image == null || data == null) {
            imageProxy.close()
            return
        }

        val result = try {
            decode(data, image)
        } catch (e: NotFoundException) {
            Timber.d(e)
            null
        }

        imageProxy.close()

        if (result == null) {
            return
        }

        callback(result)
    }

    private fun decode(data: ByteArray?, image: Image): Result? {
        return reader.decodeWithState(
            BinaryBitmap(
                HybridBinarizer(
                    PlanarYUVLuminanceSource(
                        data,
                        image.width,
                        image.height,
                        0,
                        0,
                        image.width,
                        image.height,
                        false
                    )
                )
            )
        )
    }

    private fun extractArray(image: Image?): ByteArray? {
        val buffer = image?.planes?.firstOrNull { it.buffer != null }?.buffer ?: return null
        return ByteArray(buffer.capacity()).also { buffer.get(it) }
    }

}