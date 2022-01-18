/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.rss.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.rss.R
import jp.toastkid.rss.RssReaderFragmentViewModel
import jp.toastkid.rss.databinding.ItemRssListBinding
import jp.toastkid.rss.model.Item

/**
 * @author toastkidjp
 */
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val viewModel: RssReaderFragmentViewModel?
) : ListAdapter<Item, ViewHolder>(
    CommonItemCallback.with({ a, b -> a.hashCode() == b.hashCode() }, { a, b -> a == b })
) {

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var binding: ItemRssListBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        preferenceApplier = PreferenceApplier(parent.context)
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.setIconColor(preferenceApplier.colorPair().bgColor())
        holder.setTitle(item.title)
        holder.setUrl(item.link)
        holder.setContent(item.content.toString())
        holder.setDate(item.date)
        holder.setSource(item.source)
        holder.itemView.setOnClickListener {
            viewModel?.itemClick(item.link)
        }
        holder.itemView.setOnLongClickListener {
            viewModel?.itemClick(item.link, true)
            true
        }
    }

    fun addAll(items: MutableList<Item>?) {
        items?.let {
            items.sortByDescending { item -> item.date }
            submitList(items)
        }
    }

    companion object {
        @LayoutRes
        private val LAYOUT_ID = R.layout.item_rss_list
    }
}