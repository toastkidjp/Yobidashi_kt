/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.barcode

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.PopupBarcodeResultBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * Popup for showing barcode reader's result.
 *
 * @param context [Context]
 * @author toastkidjp
 */
class BarcodeReaderResultPopup(context: Context) {

    /**
     * Popup window.
     */
    private val popupWindow = PopupWindow(context)

    /**
     * View binding.
     */
    private val binding: PopupBarcodeResultBinding

    /**
     * ViewModel of this popup.
     */
    private var popupViewModel: BarcodeReaderResultPopupViewModel? = null

    /**
     * Animation of slide up bottom.
     */
    private val slideUpBottom by lazy { AnimationUtils.loadAnimation(context, R.anim.slide_up) }

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, null, false)
        binding.popup = this

        popupWindow.contentView = binding.root

        popupWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.transparent)))

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        popupWindow.setOnDismissListener {
            binding?.result?.text = ""
        }

        if (context is FragmentActivity) {
            popupViewModel =
                    ViewModelProviders.of(context).get(BarcodeReaderResultPopupViewModel::class.java)

        }
    }

    /**
     * Set current colors.
     *
     * @param colorPair current color pair
     */
    fun onResume(colorPair: ColorPair) {
        binding.let {
            it.resultArea.setBackgroundColor(colorPair.bgColor())
            Colors.setColors(it.clip, colorPair)
            Colors.setColors(it.share, colorPair)
            Colors.setColors(it.open, colorPair)
            Colors.setColors(it.result, colorPair)
        }
    }


    /**
     * Copy result text to clipboard.
     */
    fun clip() {
        popupViewModel?.clip?.postValue(currentText())
    }

    /**
     * Share result text.
     */
    fun share() {
        popupViewModel?.share?.postValue(currentText())
    }

    /**
     * Open result text with browser.
     */
    fun open() {
        popupViewModel?.open?.postValue(currentText())
    }

    /**
     * Get current text.
     *
     * @return current text
     */
    fun currentText() = binding.result.text?.toString()

    /**
     * Show popup with parent-view and text.
     *
     * @param parent Parent View of this popup window
     * @param text Result text string.
     */
    fun show(parent: View, text: String) {
        @Suppress("UsePropertyAccessSyntax")
        binding.result.setText(text)
        binding.root.startAnimation(slideUpBottom)
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0)
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.popup_barcode_result
    }
}