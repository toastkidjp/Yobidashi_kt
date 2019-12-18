/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.notification.widget

import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import jp.toastkid.yobidashi.libs.VectorToBitmap

/**
 * @author toastkidjp
 */
class IconInitializer(private val vectorToBitmap: VectorToBitmap) {

    operator fun invoke(
            remoteViews: RemoteViews,
            @ColorInt fontColor: Int,
            @DrawableRes iconResourceId: Int,
            @IdRes viewId: Int
    ) {
        val bitmap = vectorToBitmap(iconResourceId)
        remoteViews.setImageViewBitmap(viewId, bitmap)
        remoteViews.setInt(viewId, METHOD_NAME_SET_COLOR_FILTER, fontColor)
    }

    companion object {

        /**
         * Method name.
         */
        private const val METHOD_NAME_SET_COLOR_FILTER: String = "setColorFilter"
    }
}