/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.trend

import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.toastkid.yobidashi.databinding.ItemTrendSimpleBinding

/**
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemTrendSimpleBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(trend: Trend) {
        binding.searchSuggestText.text = trend.title
        Glide.with(binding.image).asDrawable().load(trend.image.toUri()).into(binding.image)
        //binding.searchSuggestAdd.isVisible = false
    }

    fun setOnAdd(function: (String) -> Unit) {
        binding.searchSuggestAdd.setOnClickListener {
            function(binding.searchSuggestText.text.toString())
        }
    }

}