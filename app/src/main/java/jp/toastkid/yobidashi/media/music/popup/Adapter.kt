/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.music.popup

import android.content.res.Resources
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemMediaListBinding
import java.util.Random

/**
 * @author toastkidjp
 */
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val preferenceApplier: PreferenceApplier,
        resources: Resources,
        private val mediaPlayerPopupViewModel: MediaPlayerPopupViewModel?
) : RecyclerView.Adapter<ViewHolder>() {

    private lateinit var binding: ItemMediaListBinding

    private val items = mutableListOf<MediaBrowserCompat.MediaItem>()

    private val iconWidth = resources.getDimensionPixelSize(ICON_WIDTH_ID)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, parent, false)

        val placeholder = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_music)
        if (placeholder != null) {
            DrawableCompat.setTint(placeholder, preferenceApplier.color)
        }

        return ViewHolder(binding, placeholder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position % items.size)
        holder.bindText(item.description)

        val iconColor = preferenceApplier.colorPair().bgColor()
        holder.setLyricsIconColor(iconColor)

        holder.loadIcon(item.description.iconUri, iconWidth)

        // TODO Use data binding
        holder.setOnClickListener(View.OnClickListener {
            mediaPlayerPopupViewModel?.clickItem(item)
        })

        holder.setOnLyricsClickListener(View.OnClickListener {
            item.description.title?.also { lyrics ->
                mediaPlayerPopupViewModel?.clickLyrics(lyrics.toString())
            }
        })
    }

    override fun getItemCount() = items.size * 20

    fun mediumPosition() = itemCount / 2

    fun add(item: MediaBrowserCompat.MediaItem) {
        items.add(item)
    }

    fun clear() {
        items.clear()
    }

    fun random() = items[Random().nextInt(itemCount) % items.size]

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_media_list

        @DimenRes
        private const val ICON_WIDTH_ID = R.dimen.music_list_icon_width

    }
}