/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.music.popup

import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import coil.load
import jp.toastkid.yobidashi.databinding.ItemMediaListBinding

/**
 * @author toastkidjp
 */
class ViewHolder(
        private val binding: ItemMediaListBinding,
        private val placeholder: Drawable?
) : RecyclerView.ViewHolder(binding.root) {

    fun bindText(description: MediaDescriptionCompat) {
        binding.title.text = description.title
        binding.artist.text = description.subtitle
    }

    fun setLyricsIconColor(@ColorInt color: Int) {
        binding.lyrics.setColorFilter(color)
    }

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        binding.root.setOnClickListener(onClickListener)
    }

    fun setOnLyricsClickListener(onClickListener: View.OnClickListener) {
        binding.lyrics.setOnClickListener(onClickListener)
    }

    fun loadIcon(uri: Uri?, iconWidth: Int) {
        binding.icon.load(uri) {
            placeholder(placeholder)
            size(iconWidth)
        }
    }
}