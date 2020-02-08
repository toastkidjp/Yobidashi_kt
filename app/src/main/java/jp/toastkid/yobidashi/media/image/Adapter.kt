/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemImageThumbnailsBinding

/**
 * RecyclerView's adapter.
 *
 * @author toastkidjp
 */
internal class Adapter(
        private val fragmentManager: FragmentManager?,
        private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private val images = mutableListOf<Image>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = DataBindingUtil.inflate<ItemImageThumbnailsBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_image_thumbnails,
                parent,
                false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.applyContent(images.get(position)) {
            if (it.isBucket) {
                onClick(it.name)
            } else {
                ImagePreviewDialogFragment.withImage(it)
                        .show(fragmentManager, ImagePreviewDialogFragment::class.java.simpleName)
            }
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    fun add(image: Image) {
        images.add(image)
    }

    fun clear() {
        images.clear()
    }

    fun isBucketMode() = images.isNotEmpty() && images.get(0).isBucket
}