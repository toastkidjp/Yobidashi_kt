/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.rss.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.lib.view.swipe.Removable
import jp.toastkid.rss.R
import jp.toastkid.rss.databinding.ItemRssSettingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class Adapter(private val preferenceApplier: PreferenceApplier)
    : ListAdapter<String, ViewHolder>(
    CommonItemCallback.with({ a, b -> a.hashCode() == b.hashCode() }, { a, b -> a == b })
    ), Removable {

    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemRssSettingBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_rss_setting,
                parent,
                false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.setUrl(item)
        holder.setDeleteAction {
            removeItem(item)
        }
    }

    fun replace(readerTargets: Iterable<String>) {
        submitList(readerTargets.toList())
    }

    /**
     * Remove item with position.
     *
     * @param position
     * @return [Job]
     */
    override fun removeAt(position: Int): Job {
        val item = getItem(position)
        removeItem(item)
        return Job()
    }

    private fun removeItem(item: String) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO)  { preferenceApplier.removeFromRssReaderTargets(item) }

            val copy = mutableListOf<String>().also { it.addAll(currentList) }
            copy.remove(item)
            submitList(copy)
        }
    }

}