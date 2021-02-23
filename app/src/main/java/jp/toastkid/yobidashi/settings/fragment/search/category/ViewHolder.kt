/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.fragment.search.category

import androidx.recyclerview.widget.RecyclerView
import coil.load
import jp.toastkid.yobidashi.databinding.ItemSearchCategorySelectionBinding

class ViewHolder(private val binding: ItemSearchCategorySelectionBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SearchCategorySelection) {
        binding.check.isChecked = item.checked
        binding.icon.load(item.searchCategory.iconId)
        binding.text.setText(item.searchCategory.id)
    }

    fun setTapListener(action: () -> Boolean) {
        binding.root.setOnClickListener {
            val newState = action()
            binding.check.isChecked = newState
        }
    }

}