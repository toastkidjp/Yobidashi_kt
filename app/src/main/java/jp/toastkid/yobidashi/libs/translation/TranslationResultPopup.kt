/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.translation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.clip.Clipboard

/**
 * Popsup for showing translation-result.
 *
 * @param context [Context]
 * @author toastkidjp
 */
class TranslationResultPopup(context: Context) {

    /**
     * Window.
     */
    private val popupWindow: PopupWindow = PopupWindow(context)

    /**
     * Content area.
     */
    private var textView: TextView

    /**
     * Enter animation.
     */
    private val slideUpFromBottom
            = AnimationUtils.loadAnimation(context, R.anim.slide_up)

    init {
        @SuppressLint("InflateParams")
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_translated, null)
        popupView.findViewById<View>(R.id.close).setOnClickListener {
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        }

        textView = popupView.findViewById(R.id.content)

        popupView.findViewById<View>(R.id.clip).setOnClickListener {
            Clipboard.clip(context, textView.text?.toString() ?: "")
        }

        popupWindow.contentView = popupView

        popupWindow.setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(context, R.color.transparent))
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height =
                context.resources.getDimensionPixelSize(R.dimen.translation_popup_height)
    }

    /**
     * Show this popup with parent view and content.
     *
     * @param parent [View]
     * @param content [String]
     */
    fun show(parent: View, content: String) {
        textView.setText(content)
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0)
        popupWindow.contentView.startAnimation(slideUpFromBottom)
    }

    /**
     * Hide this popup.
     */
    fun hide() {
        popupWindow.takeIf { it.isShowing }?.dismiss()
    }

}