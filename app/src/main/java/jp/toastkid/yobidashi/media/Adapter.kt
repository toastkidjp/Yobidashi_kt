/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.graphics.BitmapFactory
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemMediaListBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber
import java.io.FileNotFoundException

/**
 * @author toastkidjp
 */
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val preferenceApplier: PreferenceApplier,
        private val onClickItem: (MediaBrowserCompat.MediaItem) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private lateinit var binding: ItemMediaListBinding

    private lateinit var albumArtFinder: AlbumArtFinder

    private val items = mutableListOf<MediaBrowserCompat.MediaItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, parent, false)
        albumArtFinder = AlbumArtFinder(binding.root.context.contentResolver)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.bindText(item.description)

        val albumArtOrNull = try {
            BitmapFactory.decodeStream(binding.root.context.contentResolver.openInputStream(item.description.iconUri))
        } catch (e: FileNotFoundException) {
            Timber.w(e)
            null
        }
        if (albumArtOrNull == null) {
            holder.setIconColor(preferenceApplier.colorPair().bgColor())
            holder.setIconId(R.drawable.ic_music)
        } else {
            holder.setIcon(albumArtOrNull)
        }

        holder.setOnClickListener(View.OnClickListener {
            onClickItem(item)
        })
    }

    override fun getItemCount() = items.size

    fun add(item: MediaBrowserCompat.MediaItem) {
        items.add(item)
    }

    fun clear() {
        items.clear()
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_media_list

    }
}