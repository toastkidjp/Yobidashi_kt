/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview.attach

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.PopupAttachMenuBinding

/**
 * @author toastkidjp
 */
class AttachMenuPopup(context: Context, menuActionUseCase: MenuActionUseCase) {

    private val popupWindow = PopupWindow(context)

    private val binding: PopupAttachMenuBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.popup_attach_menu,
            null,
            false
    )

    init {
        binding.useCase = menuActionUseCase

        popupWindow.contentView = binding.root
        popupWindow.isOutsideTouchable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    fun show(view: View) {
        popupWindow.showAsDropDown(view, 100, -300)
    }

}