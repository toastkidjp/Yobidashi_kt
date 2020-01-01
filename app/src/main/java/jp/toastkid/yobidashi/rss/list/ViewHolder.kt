/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.list

import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemRssListBinding

/**
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemRssListBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setIconColor(@ColorInt color: Int) {
        binding.icon.setColorFilter(color)
    }

    fun setTitle(title: String) {
        binding.title.text = title
    }

    fun setUrl(link: String) {
        binding.url.text = link
    }

    fun setContent(content: String) {
        binding.content.isVisible = content.isNotBlank()
        binding.content.text = content
    }

    fun setDate(date: String) {
        binding.time.isVisible = date.isNotBlank()
        binding.time.text = date
    }

}