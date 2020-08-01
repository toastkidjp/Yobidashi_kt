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
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.article_viewer.R

/**
 * @author toastkidjp
 */
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val viewModel: SubheadDialogFragmentViewModel
) : RecyclerView.Adapter<ViewHolder>() {

    private val subheads = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                layoutInflater.inflate(ITEM_ID, parent, false)
                        as? TextView ?: TextView(parent.context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = subheads[position]
        holder.setText(item)
        holder.itemView.setOnClickListener {
            viewModel.subhead(item)
        }
    }

    override fun getItemCount() = subheads.size

    fun addAll(items: List<String>?) {
        items?.let {
            subheads.addAll(it)
        }
    }

    companion object {

        @LayoutRes
        private val ITEM_ID = R.layout.item_subhead_dialog

    }
}