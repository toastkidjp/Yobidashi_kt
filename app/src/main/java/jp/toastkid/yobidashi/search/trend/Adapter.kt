/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.trend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.lib.view.list.CommonItemCallback
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.SearchFragmentViewModel

/**
 * @author toastkidjp
 */
class Adapter : ListAdapter<Trend, ViewHolder>(
    CommonItemCallback.with<Trend>({ a, b -> a.hashCode() == b.hashCode() }, { a, b -> a == b })
) {

    private val items = mutableListOf<Trend>()

    private var viewModel: SearchFragmentViewModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_trend_simple, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            viewModel?.search(item.link)
        }
        holder.itemView.setOnLongClickListener {
            viewModel?.searchOnBackground(item.title)
            true
        }
        holder.setOnAdd {
            viewModel?.putQuery(it)
        }
    }

    fun replace(trends: List<Trend>?) {
        trends ?: return

        submitList(trends)
    }

    fun isNotEmpty(): Boolean {
        return currentList.isNotEmpty()
    }

    fun setViewModel(viewModel: SearchFragmentViewModel) {
        this.viewModel = viewModel
    }

}