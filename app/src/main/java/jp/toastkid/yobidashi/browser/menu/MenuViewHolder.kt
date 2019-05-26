/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.menu

import android.graphics.Color
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemHomeMenuBinding
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * @author toastkidjp
 */
class MenuViewHolder(private val binding: ItemHomeMenuBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun setText(@StringRes titleId: Int) {
        binding.text.setText(titleId)
    }

    fun setImage(@DrawableRes iconId: Int) {
        binding.image.setImageResource(iconId)
    }

    fun setColorPair(pair: ColorPair, useIconColorFilter: Boolean = true) {
        itemView.setBackgroundColor(pair.bgColor())

        if (binding.count.isVisible) {
            binding.count.setTextColor(pair.fontColor())
        }

        binding.text.setTextColor(pair.fontColor())

        binding.image.setColorFilter(if (useIconColorFilter) pair.fontColor() else Color.TRANSPARENT)
    }

    fun setOnClick(onClick: View.OnClickListener) {
        itemView.setOnClickListener(onClick)
    }

    fun setOnLongClick(onLongClick: View.OnLongClickListener) {
        itemView.setOnLongClickListener(onLongClick)
    }

    fun setCount(tabCount: Int) {
        binding.count.text = tabCount.toString()
        binding.count.visibility = View.VISIBLE
    }

    fun hideCount() {
        binding.count.visibility = View.GONE
    }
}
