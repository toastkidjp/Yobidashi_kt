/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.barcode.detection

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class Adapter(context: Context) : RecyclerView.Adapter<ViewHolder>() {

    private val detections = mutableListOf<Detection>()

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(
            DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.item_detection_result,
                    null,
                    false
            )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detection = detections[position]
        holder.setBitmap(detection.bitmap)
        holder.setCategory(detection.category)
    }

    override fun getItemCount() = detections.size

    fun add(detection: Detection) {
        detections.add(detection)
    }

    fun isEmpty() = detections.isEmpty()
}