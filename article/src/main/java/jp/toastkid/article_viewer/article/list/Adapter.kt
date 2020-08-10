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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.list.paging.SimpleComparator
import jp.toastkid.article_viewer.article.list.sort.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Collections

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
    private val repository: ArticleRepository,
    private val onClick: (String) -> Unit,
    private val onLongClick: (String) -> Unit
) : PagingDataAdapter<SearchResult, ViewHolder>(SimpleComparator()) {

    private val items: MutableList<SearchResult> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            layoutInflater.inflate(ITEM_LAYOUT_ID, parent, false),
            onClick,
            onLongClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position) ?: return
        holder.bind(result)
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

    fun all() {
        CoroutineScope(Dispatchers.IO).launch {
            Pager(PagingConfig(pageSize = 50, enablePlaceholders = true)) {
                repository.getAll()
            }
                    .flow
                    .collectLatest {
                        submitData(it)
                    }
        }
    }

    fun search(biGram: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Pager(PagingConfig(pageSize = 50, enablePlaceholders = true)) {
                repository.search(biGram)
            }
                    .flow
                    .collectLatest {
                        submitData(it)
                    }
        }
    }

    fun filter(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Pager(PagingConfig(pageSize = 50, enablePlaceholders = true)) {
                repository.filter(query)
            }
                    .flow
                    .collectLatest {
                        submitData(it)
                    }
        }
    }

    fun sort(sort: Sort) {
        Collections.sort(items, sort.comparator)
        notifyDataSetChanged()
    }

    companion object {

        @LayoutRes
        private val ITEM_LAYOUT_ID = R.layout.item_result
    }
}