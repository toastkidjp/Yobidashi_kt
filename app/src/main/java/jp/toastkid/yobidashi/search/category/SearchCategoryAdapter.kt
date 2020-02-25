/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.SearchCategory

/**
 * @author toastkidjp
 */
class SearchCategoryAdapter(context: Context): BaseAdapter() {
    private val inflater = LayoutInflater.from(context)

    private val searchCategories = SearchCategory.values()

    override fun getCount(): Int = searchCategories.size

    override fun getItem(position: Int): SearchCategory = searchCategories[position]

    override fun getItemId(position: Int): Long = searchCategories[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val searchCategory = searchCategories[position]

        if (convertView == null) {
            val view = inflater.inflate(LAYOUT_ID, parent, false)
            val icon = view.findViewById<ImageView>(R.id.search_category_image)
            val text = view.findViewById<TextView>(R.id.search_category_text)
            val viewHolder = ViewHolder(icon, text)
            view.tag = viewHolder
            viewHolder.bindItem(searchCategory)
            return view
        }

        val viewHolder = (convertView.tag as ViewHolder)
        viewHolder.bindItem(searchCategory)
        return convertView
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_spinner_search_category

    }
}