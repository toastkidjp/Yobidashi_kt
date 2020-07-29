/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.media.image.Image

/**
 * @author toastkidjp
 */
class Adapter : RecyclerView.Adapter<ViewHolder>() {

    private val images = mutableListOf<Image>()

    private val imageViewFactory = ImageViewFactory()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(imageViewFactory(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setImage(images[position].path)
    }

    override fun getItemCount(): Int = images.size

    fun setImages(images: List<Image>) {
        this.images.addAll(images)
    }

    fun getPath(position: Int): String? {
        return images.get(position).path
    }

}