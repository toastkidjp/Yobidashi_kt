/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.color

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * @author toastkidjp
 */
class LinkColorGenerator {

    @ColorInt
    operator fun invoke(@ColorInt baseColor: Int): Int {
        return Color.rgb(Color.green(baseColor), Color.blue(baseColor), Color.red(baseColor))
    }

}