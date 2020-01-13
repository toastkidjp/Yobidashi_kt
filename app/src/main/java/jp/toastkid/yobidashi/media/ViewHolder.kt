/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.databinding.ItemMediaListBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author toastkidjp
 */
class ViewHolder(private val binding: ItemMediaListBinding) : RecyclerView.ViewHolder(binding.root) {

    private val dateFormatHolder = object : ThreadLocal<DateFormat>() {
        @SuppressLint("SimpleDateFormat")
        override fun initialValue(): DateFormat? {
            return SimpleDateFormat("yyyy/MM/dd(E)HH:mm:ss")
        }
    }

    fun bindText(audio: Audio) {
        binding.title.text = audio.title
        binding.time.text = dateFormatHolder.get()
                ?.format(Date().also { it.time = (audio.date ?: 0) * 1000L })
        binding.artist.setText("${audio.artist} / ${audio.album}")
    }

    fun setIconColor(@ColorInt color: Int) {
        binding.icon.setColorFilter(color)
    }

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        binding.root.setOnClickListener(onClickListener)
    }

    fun setIcon(albumArtOrNull: Bitmap) {
        setIconColor(Color.TRANSPARENT)
        binding.icon.setImageBitmap(albumArtOrNull)
    }

    fun setIconId(@DrawableRes iconId: Int) {
        binding.icon.setImageResource(iconId)
    }
}