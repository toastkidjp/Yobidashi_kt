/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemImageThumbnailsBinding
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.media.image.Image
import jp.toastkid.yobidashi.media.image.preview.ImagePreviewDialogFragment

/**
 * RecyclerView's adapter.
 *
 * @author toastkidjp
 */
internal class Adapter(
        private val fragmentManager: FragmentManager?,
        private val imageViewerFragmentViewModel: ImageViewerFragmentViewModel?
) : RecyclerView.Adapter<ViewHolder>() {

    private val images = mutableListOf<Image>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = DataBindingUtil.inflate<ItemImageThumbnailsBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_image_thumbnails,
                parent,
                false
        )
        val placeholder = ContextCompat.getDrawable(parent.context, R.drawable.ic_image_search)
        if (placeholder != null) {
            DrawableCompat.setTint(placeholder, PreferenceApplier(parent.context).color)
        }
        return ViewHolder(itemBinding, placeholder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = images.get(position)
        holder.applyContent(image) {
            if (it.isBucket) {
                imageViewerFragmentViewModel?.click(it.name)
            } else {
                val fragmentManager = fragmentManager ?: return@applyContent
                ImagePreviewDialogFragment.withImages(images, position)
                        .show(fragmentManager, ImagePreviewDialogFragment::class.java.simpleName)
            }
        }
        holder.itemView.setOnLongClickListener {
            val excludingId = image.makeExcludingId()

            if (excludingId.isNullOrBlank()) {
                return@setOnLongClickListener true
            }

            imageViewerFragmentViewModel?.longClick(excludingId)
            return@setOnLongClickListener true
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

    fun isBucketMode() = images.isNotEmpty() && images[0].isBucket
}