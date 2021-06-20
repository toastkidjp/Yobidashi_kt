/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view.filter.color

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.FrameLayout
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class ForegroundColorFilterUseCase(
    private val preferenceApplier: PreferenceApplier
) {

    operator fun invoke(frameLayout: FrameLayout) {
        val filterDrawable =
            if (preferenceApplier.useColorFilter())
                ColorDrawable(preferenceApplier.filterColor(Color.TRANSPARENT))
            else
                null
        frameLayout.foreground = filterDrawable
    }

}