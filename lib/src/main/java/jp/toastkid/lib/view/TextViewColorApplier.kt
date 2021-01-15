/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.TextView
import androidx.annotation.ColorInt

/**
 * @author toastkidjp
 */
class TextViewColorApplier {

    /**
     * Apply button color to multiple [TextView].
     *
     * @param fontColor [ColorInt]
     * @param textViews multiple [TextView]
     */
    operator fun invoke(@ColorInt fontColor: Int, vararg textViews: TextView?) {
        textViews.filterNotNull().forEach { textView ->
            textView.setTextColor(fontColor)
            textView.compoundDrawables.forEach {
                it?.colorFilter = PorterDuffColorFilter(fontColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }
}