/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemRssSettingBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.history.Removable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class Adapter(private val preferenceApplier: PreferenceApplier) : RecyclerView.Adapter<ViewHolder>(), Removable {

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
        val item = items.get(position)
        holder.setUrl(item)
        holder.setDeleteAction {
            preferenceApplier.removeFromRssReaderTargets(item)
            items.remove(item)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount() = items.size

    fun replace(readerTargets: Iterable<String>) {
        items.clear()
        items.addAll(readerTargets)
    }

    /**
     * Remove item with position.
     *
     * @param position
     * @return [Job]
     */
    override fun removeAt(position: Int): Job {
        val item = items[position]
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO)  { preferenceApplier.removeFromRssReaderTargets(item) }

            items.remove(item)
            notifyItemRemoved(position)
        }
        return Job()
    }

}