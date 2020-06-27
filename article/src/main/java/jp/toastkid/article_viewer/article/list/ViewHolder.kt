/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.article_viewer.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author toastkidjp
 */
class ViewHolder(
    private val view: View,
    private val onClick: (String) -> Unit,
    private val onLongClick: (String) -> Unit
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
            "Last updated: ${DATE_FORMAT.get()?.format(Date().also { it.time = result.lastModified })}" +
                    " / ${result.length} chars"
    }

    companion object {
        private val DATE_FORMAT = object : ThreadLocal<DateFormat>() {
            override fun initialValue() = SimpleDateFormat("yyyy/MM/dd(E) HH:mm:ss", Locale.JAPAN)
        }
    }
}