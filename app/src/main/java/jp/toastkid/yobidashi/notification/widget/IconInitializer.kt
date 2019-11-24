/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.notification.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * @author toastkidjp
 */
class IconInitializer(private val context: Context) {

    operator fun invoke(remoteViews: RemoteViews, fontColor: Int, iconResourceId: Int, viewId: Int) {
        val bitmap = vectorToBitmap(iconResourceId)
        remoteViews.setImageViewBitmap(viewId, bitmap)
        remoteViews.setInt(viewId, METHOD_NAME_SET_COLOR_FILTER, fontColor)
    }

    private fun vectorToBitmap(@DrawableRes resVector: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, resVector)
        val b = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        drawable.setBounds(0, 0, c.getWidth(), c.getHeight())
        drawable.draw(c)
        return b
    }

    companion object {

        /**
         * Method name.
         */
        private const val METHOD_NAME_SET_COLOR_FILTER: String = "setColorFilter"
    }
}