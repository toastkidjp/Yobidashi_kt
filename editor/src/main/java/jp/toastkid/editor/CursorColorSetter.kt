/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.editor

import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import timber.log.Timber

/**
 * @author toastkidjp
 */
class CursorColorSetter {

    operator fun invoke(editText: EditText, @ColorInt newColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setColor(editText, newColor)
            return
        }

        try {
            val editor = extractEditor(editText)
            val field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field.set(editor, makeDrawables(editText, newColor))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setColor(editText: EditText, newColor: Int) {
        editText.textCursorDrawable?.let { drawable ->
            DrawableCompat.setTint(drawable, newColor)
            editText.textCursorDrawable = drawable
        }

        if (editText.textCursorDrawable == null) {
            editText.textCursorDrawable = ColorDrawable(newColor)
        }

        editText.textSelectHandleLeft?.let {
            DrawableCompat.setTint(it, newColor)
            editText.setTextSelectHandleLeft(it)
        }

        editText.textSelectHandleRight?.let {
            DrawableCompat.setTint(it, newColor)
            editText.setTextSelectHandleRight(it)
        }

        editText.textSelectHandle?.let {
            DrawableCompat.setTint(it, newColor)
            editText.setTextSelectHandle(it)
        }
    }

    private fun makeDrawables(editText: EditText, @ColorInt newColor: Int): Array<Drawable>? {
        val drawable = try {
            ContextCompat.getDrawable(editText.context, extractDrawableId(editText))
        } catch (e: Resources.NotFoundException) {
            Timber.d(e)
            null
        } ?: return null
        drawable.colorFilter = PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_IN)
        return arrayOf(drawable, drawable)
    }

    private fun extractDrawableId(editText: EditText): Int {
        val field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
        field.isAccessible = true
        return field.getInt(editText)
    }

    private fun extractEditor(editText: EditText): Any {
        val field = TextView::class.java.getDeclaredField("mEditor")
        field.isAccessible = true
        return field.get(editText)
    }
}