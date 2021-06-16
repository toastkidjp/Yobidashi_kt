/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.article_viewer.R

/**
 * @author toastkidjp
 */
class ViewHolder(
    private val view: View,
    private val onClick: (String) -> Unit,
    private val onLongClick: (String) -> Unit,
    private val onMenuClick: (View, SearchResult) -> Unit,
    @ColorInt private val menuColor: Int
) : RecyclerView.ViewHolder(view) {

    @SuppressLint("SetTextI18n")
    fun bind(result: SearchResult) {
        view.findViewById<TextView>(R.id.main_text).text = result.title
        view.setOnClickListener { onClick(result.title) }
        view.setOnLongClickListener {
            onLongClick(result.title)
            true
        }
        view.findViewById<TextView>(R.id.sub_text).text =
            "Last updated: ${DateFormat.format("yyyy/MM/dd(E) HH:mm:ss", result.lastModified)}" +
                    " / ${result.length}"

        view.findViewById<ImageView>(R.id.menu).also {
            it.setColorFilter(menuColor)
            it.setOnClickListener { v ->
                onMenuClick(v, result)
            }
        }
    }

}