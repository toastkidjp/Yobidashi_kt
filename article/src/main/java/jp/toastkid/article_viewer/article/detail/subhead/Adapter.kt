/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail.subhead

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * @author toastkidjp
 */
class Adapter(private val layoutInflater: LayoutInflater) : RecyclerView.Adapter<ViewHolder>() {

    private val subheads = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                        as? TextView ?: TextView(parent.context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = subheads[position]
        holder.setText(item)
    }

    override fun getItemCount() = subheads.size

    fun addAll(items: List<String>?) {
        items?.let {
            subheads.addAll(it)
        }
    }

}