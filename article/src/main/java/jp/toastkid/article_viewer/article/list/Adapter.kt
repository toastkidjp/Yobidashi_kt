/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.paging.PagingDataAdapter
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.list.paging.SimpleComparator
import jp.toastkid.lib.color.IconColorFinder

/**
 * [SearchResult] list's adapter.
 *
 * @param layoutInflater [LayoutInflater]
 * @param onClick Callback of click event
 * @param onLongClick Callback of long-click event
 * @param onMenuClick Callback of menu-click event
 *
 * @author toastkidjp
 */
class Adapter(
    private val layoutInflater: LayoutInflater,
    private val onClick: (String) -> Unit,
    private val onLongClick: (String) -> Unit,
    private val onMenuClick: (View, SearchResult) -> Unit
) : PagingDataAdapter<SearchResult, ViewHolder>(SimpleComparator()) {

    @ColorInt
    private var menuColor = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (menuColor == -1) {
            menuColor = IconColorFinder.from(parent).invoke()
        }

        return ViewHolder(
                layoutInflater.inflate(ITEM_LAYOUT_ID, parent, false),
                onClick,
                onLongClick,
                onMenuClick,
                menuColor
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position) ?: return
        holder.bind(result)
    }

    companion object {

        @LayoutRes
        private val ITEM_LAYOUT_ID = R.layout.item_result
    }
}