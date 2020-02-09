/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.graphics.Bitmap
import android.graphics.Color
import android.support.v4.media.MediaDescriptionCompat
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemMediaListBinding

/**
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemMediaListBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindText(description: MediaDescriptionCompat) {
        binding.title.text = description.title
        binding.artist.setText(description.subtitle)
    }

    fun setLyricsIconColor(@ColorInt color: Int) {
        binding.lyrics.setColorFilter(color)
    }

    fun setIconColor(@ColorInt color: Int) {
        binding.icon.setColorFilter(color)
    }

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        binding.root.setOnClickListener(onClickListener)
    }

    fun setOnLyricsClickListener(onClickListener: View.OnClickListener) {
        binding.lyrics.setOnClickListener(onClickListener)
    }

    fun setIcon(albumArtOrNull: Bitmap) {
        setIconColor(Color.TRANSPARENT)
        binding.icon.setImageBitmap(albumArtOrNull)
    }

    fun setIconId(@DrawableRes iconId: Int) {
        binding.icon.setImageResource(iconId)
    }
}