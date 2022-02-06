/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.barcode.generator

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class BarcodeGenerator(private val barcodeEncoder : BarcodeEncoder = BarcodeEncoder()) {

    @WorkerThread
    operator fun invoke(url: String?, size: Int): Bitmap {
        return barcodeEncoder
            .encodeBitmap(url, BarcodeFormat.QR_CODE, size, size)
    }

}