/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemSettingImageExcludingBinding
import jp.toastkid.lib.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class Adapter(
        private val preferenceApplier: PreferenceApplier,
        private val viewModel: ExcludingSettingFragmentViewModel
) : RecyclerView.Adapter<ViewHolder>() {

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
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setText(items.get(position))
    }

    override fun getItemCount() = items.size

    fun addAll(excludedItems: Set<String>?) {
        if (excludedItems == null) {
            return
        }
        items.addAll(excludedItems)
    }

    fun removeAt(position: Int) {
        items.get(position)
    }

}