/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.barcode.model

import android.graphics.ImageFormat
import android.media.Image
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import timber.log.Timber

class BarcodeAnalyzerImplementation {

    private val acceptableFormats =
        mutableListOf(ImageFormat.YUV_420_888, ImageFormat.YUV_422_888, ImageFormat.YUV_444_888)

    private val reader: MultiFormatReader = MultiFormatReader().also {
        it.setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                    BarcodeFormat.QR_CODE
                )
            )
        )
    }

    operator fun invoke(image: Image?, format: Int): String? {
        if (image == null || acceptableFormats.contains(format).not()) {
            return null
        }

        val data = extractArray(image) ?: return null

        val result = try {
            reader.decodeWithState(
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
        } catch (e: NotFoundException) {
            Timber.d(e)
            null
        } ?: return null

        return result.text
    }

    private fun extractArray(image: Image?): ByteArray? {
        val buffer = image?.planes?.firstOrNull { it.buffer != null }?.buffer ?: return null
        return ByteArray(buffer.capacity()).also { buffer.get(it) }
    }

}