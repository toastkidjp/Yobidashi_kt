/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.window

import android.annotation.TargetApi
import android.app.Activity
import android.graphics.Rect
import android.os.Build

class WindowRectCalculatorCompat {

    @TargetApi(Build.VERSION_CODES.R)
    @Suppress("DEPRECATION")
    operator fun invoke(activity: Activity?, sdkVersion: Int = Build.VERSION.SDK_INT): Rect? {
        return if (sdkVersion >= Build.VERSION_CODES.R) {
            activity?.windowManager?.currentWindowMetrics?.bounds
        } else {
            val rect = Rect()
            activity?.windowManager?.defaultDisplay?.getRectSize(rect)
            return rect
        }
    }

}