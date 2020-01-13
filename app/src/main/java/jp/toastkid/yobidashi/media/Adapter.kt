/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemMediaListBinding
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import java.io.FileNotFoundException

/**
 * @author toastkidjp
 */
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val preferenceApplier: PreferenceApplier
) : RecyclerView.Adapter<ViewHolder>() {

    private lateinit var binding: ItemMediaListBinding

    private val items = mutableListOf<Audio>()

    private val mediaPlayer = MediaPlayer();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_media_list, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.bindText(item)
        val albumArtOrNull = extractAlbumArt(item.albumId ?: 0L)
        if (albumArtOrNull == null) {
            holder.setIconColor(preferenceApplier.colorPair().bgColor())
            holder.setIconId(R.drawable.ic_music)
        } else {
            holder.setIcon(albumArtOrNull)
        }
        holder.setOnClickListener(View.OnClickListener {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(binding.root.context, item.path?.toUri())
            mediaPlayer.prepare()
            mediaPlayer.start()
        })
    }

    override fun getItemCount() = items.size

    fun add(item: Audio) {
        items.add(item)
    }

    fun clear() {
        items.clear()
    }

    /**
     *
     * @return displayStop
     */
    fun switch(): Boolean {
        return if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            false
        } else {
            mediaPlayer.start()
            true
        }
    }

    fun reset() {
        mediaPlayer.reset()
    }

    fun dispose() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    private fun extractAlbumArt(id: Long): Bitmap? {
        val albumArtUri = "content://media/external/audio/albumart".toUri()
        val album1Uri = ContentUris.withAppendedId(albumArtUri, id)
        try {
            val contentResolver = binding.root.context.contentResolver
            return BitmapFactory.decodeStream(contentResolver.openInputStream(album1Uri))
        } catch (err: FileNotFoundException) {
            err.printStackTrace()
        }
        return null
    }
}