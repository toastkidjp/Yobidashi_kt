/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemRssListBinding
import jp.toastkid.yobidashi.rss.RssReaderFragmentViewModel
import jp.toastkid.yobidashi.rss.model.Item

/**
 * @author toastkidjp
 */
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val viewModel: RssReaderFragmentViewModel?
) : RecyclerView.Adapter<ViewHolder>() {

    private val items = mutableListOf<Item>()

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var binding: ItemRssListBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        preferenceApplier = PreferenceApplier(parent.context)
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.setIconColor(preferenceApplier.colorPair().bgColor())
        holder.setTitle(item.title)
        holder.setUrl(item.link)
        holder.setContent(item.content.toString())
        holder.setDate(item.date)
        holder.itemView.setOnClickListener {
            viewModel?.itemClick(item.link)
        }
        holder.itemView.setOnLongClickListener {
            viewModel?.itemClick(item.link, true)
            true
        }
    }

    override fun getItemCount()= items.size

    fun addAll(items: MutableList<Item>?) {
        items?.let {
            this.items.addAll(it)
            notifyDataSetChanged()
        }
    }

    companion object {
        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_rss_list
    }
}