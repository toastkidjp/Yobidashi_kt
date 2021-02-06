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
import coil.load
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemTrendSimpleBinding

/**
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemTrendSimpleBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(trend: Trend) {
        binding.searchSuggestText.text = trend.title
        binding.image.load(trend.image.toUri()) {
            placeholder(R.drawable.ic_trend_black)
            error(R.drawable.ic_trend_black)
        }
    }

    fun setOnAdd(function: (String) -> Unit) {
        binding.searchSuggestAdd.setOnClickListener {
            function(binding.searchSuggestText.text.toString())
        }
    }

}