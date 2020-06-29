/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.article_viewer.R

/**
 * [SearchResult] list's adapter.
 *
 * @param layoutInflater [LayoutInflater]
 * @param onClick Callback of click event
 * @param onLongClick Callback of long-click event
 *
 * @author toastkidjp
 */
class Adapter(
    private val layoutInflater: LayoutInflater,
    private val onClick: (String) -> Unit,
    private val onLongClick: (String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private val items: MutableList<SearchResult> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            layoutInflater.inflate(ITEM_LAYOUT_ID, parent, false),
            onClick,
            onLongClick
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /**
     * Remove all item from current list.
     */
    fun clear() {
        items.clear()
    }

    /**
     * Add new item.
     *
     * @param result new item
     */
    fun add(result: SearchResult) {
        items.add(result)
    }

    companion object {

        @LayoutRes
        private val ITEM_LAYOUT_ID = R.layout.item_result
    }
}