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

class BarcodeGenerator {

    @WorkerThread
    operator fun invoke(url: String?, size: Int): Bitmap {
        if (size <= 0) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
        }

        return BarcodeEncoder()
            .encodeBitmap(url, BarcodeFormat.QR_CODE, size, size)
    }

}