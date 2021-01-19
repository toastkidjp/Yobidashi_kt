/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.trend

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemSearchSuggestionBinding

/**
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemSearchSuggestionBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String) {
        binding.searchSuggestText.text = text
        binding.searchSuggestAdd.isVisible = false
    }

    fun setOnClick(browseCallback: (String) -> Unit) {
        binding.root.setOnClickListener {
            browseCallback(binding.searchSuggestText.text.toString())
        }
    }

    fun setOnLongClick(browseBackgroundCallback: (String) -> Unit) {
        binding.root.setOnLongClickListener {
            browseBackgroundCallback(binding.searchSuggestText.text.toString())
            true
        }
    }

}