/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.barcode

import android.app.Activity
import com.google.zxing.integration.android.IntentIntegrator
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
object LinearBarcodeReader {

    operator fun invoke(activity: Activity) {
        IntentIntegrator(activity).also {
            it.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
            it.setPrompt(activity.getString(R.string.prompt_linear_barcode))
            it.setCameraId(0)
            it.setBeepEnabled(false)
            it.setBarcodeImageEnabled(true)
            it.setOrientationLocked(false)
            it.initiateScan()
        }
    }
}