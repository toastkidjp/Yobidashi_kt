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
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class Adapter(
        private val browseCallback: (String) -> Unit,
        private val browseBackgroundCallback: (String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_search_suggestion, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items.get(position))
        holder.setOnClick(browseCallback)
        holder.setOnLongClick(browseBackgroundCallback)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun replace(trends: List<String>?) {
        trends ?: return

        items.clear()
        items.addAll(trends)
    }

    fun isNotEmpty(): Boolean {
        return items.isNotEmpty()
    }

}