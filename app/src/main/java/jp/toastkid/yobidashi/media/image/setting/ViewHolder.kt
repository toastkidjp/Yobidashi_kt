/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.setting

import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemSettingImageExcludingBinding

/**
 * @author toastkidjp
 */
class ViewHolder(
        private val binding: ItemSettingImageExcludingBinding,
        private val refresh: (String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.delete.setOnClickListener { removeItem() }
        binding.deleteButton.setOnClickListener { removeItem() }
    }

    private fun removeItem() {
        refresh(binding.text.text.toString())
    }

    fun setText(item: String) {
        binding.text.text = item
    }

}