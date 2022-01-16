/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.image.R
import jp.toastkid.image.databinding.ItemSettingImageExcludingBinding
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.list.CommonItemCallback

/**
 * @author toastkidjp
 */
class Adapter(
        private val preferenceApplier: PreferenceApplier,
        private val viewModel: ExcludingSettingFragmentViewModel
) : ListAdapter<String, ViewHolder>(
    CommonItemCallback.with<String>({ a, b -> a.hashCode() == b.hashCode() }, { a, b -> a == b })
) {

    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemSettingImageExcludingBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_setting_image_excluding,
                parent,
                false
        )
        return ViewHolder(binding) {
            items.remove(it)
            preferenceApplier.removeFromExcluding(it)

            if (items.isEmpty()) {
                viewModel.dismiss()
                return@ViewHolder
            }
            submitList(preferenceApplier.excludedItems().toList())
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setText(getItem(position))
    }

    fun addAll(excludedItems: Set<String>?) {
        if (excludedItems == null) {
            return
        }
        submitList(excludedItems.toList())
        items.addAll(excludedItems)
    }

    fun removeAt(position: Int) {
        items.get(position)
        submitList(preferenceApplier.excludedItems().toList())
    }

}