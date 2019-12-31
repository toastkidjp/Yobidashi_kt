/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.setting

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemRssSettingBinding
import jp.toastkid.yobidashi.libs.view.SwipeViewHolder

/**
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemRssSettingBinding)
    : RecyclerView.ViewHolder(binding.root), SwipeViewHolder {

    private val buttonMargin = binding.root.context.resources
            .getDimensionPixelSize(R.dimen.button_margin)

    fun setUrl(url: String) {
        binding.text.text = url
    }

    fun setDeleteAction(url: () -> Unit) {
        binding.delete.setOnClickListener {
            url()
        }
    }

    override fun isButtonVisible() = binding.delete.isVisible

    override fun showButton() {
        binding.delete.visibility = View.VISIBLE
        updateRightMargin(buttonMargin)
    }

    override fun hideButton() {
        binding.delete.visibility = View.INVISIBLE
        updateRightMargin(0)
    }

    override fun getFrontView() = binding.front

    private fun updateRightMargin(margin: Int) {
        val marginLayoutParams = binding.front.layoutParams as ViewGroup.MarginLayoutParams
        marginLayoutParams.rightMargin = margin
        binding.front.layoutParams = marginLayoutParams
        marginLayoutParams.updateMargins()
    }
}