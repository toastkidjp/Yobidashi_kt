/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.fragment.search.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.R

class Adapter(private val preferenceApplier: PreferenceApplier)
    : ListAdapter<SearchCategorySelection, ViewHolder>(
    CommonItemCallback.with<SearchCategorySelection>({ a, b -> a.searchCategory.id == b.searchCategory.id }, { a, b -> a == b })
) {

    private val initialDisables = preferenceApplier.readDisableSearchCategory()

    private val items = SearchCategory.values()
            .map { SearchCategorySelection(it, initialDisables?.contains(it.name)?.not() ?: true) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_search_category_selection,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.bind(item)
        holder.setTapListener({
            item.checked = item.checked.not()
            if (item.checked) {
                preferenceApplier.removeDisableSearchCategory(item.searchCategory.name)
            } else {
                preferenceApplier.addDisableSearchCategory(item.searchCategory.name)
            }
            return@setTapListener item.checked
        })
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun invokeCheckAll() {
        items.forEach { it.checked = true }
        notifyDataSetChanged()
    }

}