/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.list

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import coil.load
import jp.toastkid.yobidashi.databinding.ItemImageThumbnailsBinding
import jp.toastkid.yobidashi.media.image.Image
import java.io.File

/**
 * Extended of [RecyclerView.ViewHolder].
 *
 * @param binding Binding object.
 *
 * @author toastkidjp
 */
internal class ViewHolder(
        private val binding: ItemImageThumbnailsBinding,
        private val placeholder: Drawable?
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Apply file content.
     *
     * @param image image
     */
    fun applyContent(image: Image, onClick: (Image) -> Unit) {
        binding.image.load(File(image.path)) {
            placeholder(placeholder)
            size(300)
        }

        this.binding.text.text = image.makeDisplayName()
        this.binding.root.setOnClickListener {
            onClick(image)
        }
    }

}