/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.content.res.Resources
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemMediaListBinding
import jp.toastkid.yobidashi.libs.BitmapScaling
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import java.util.*

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

    private lateinit var albumArtFinder: AlbumArtFinder

    private val items = mutableListOf<MediaBrowserCompat.MediaItem>()

    private val iconWidth = resources.getDimension(ICON_WIDTH_ID).toDouble()

    private val disposables = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = DataBindingUtil.inflate(layoutInflater, LAYOUT_ID, parent, false)
        albumArtFinder = AlbumArtFinder(binding.root.context.contentResolver)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items.get(position)
        holder.bindText(item.description)

        val iconColor = preferenceApplier.colorPair().bgColor()
        holder.setLyricsIconColor(iconColor)

        Maybe.fromCallable { item.description.iconUri?.let { albumArtFinder(it) } ?: throw RuntimeException() }
                .subscribeOn(Schedulers.io())
                .map { BitmapScaling(it, iconWidth, iconWidth) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(holder::setIcon) {
                    holder.setIconColor(iconColor)
                    holder.setIconId(R.drawable.ic_music)
                }
                .addTo(disposables)

        holder.setOnClickListener(View.OnClickListener {
            mediaPlayerPopupViewModel?.clickItem(item)
        })

        holder.setOnLyricsClickListener(View.OnClickListener {
            item.description.title?.also { lyrics ->
                mediaPlayerPopupViewModel?.clickLyrics(lyrics.toString())
            }
        })
    }

    override fun getItemCount() = items.size

    fun add(item: MediaBrowserCompat.MediaItem) {
        items.add(item)
    }

    fun clear() {
        items.clear()
    }

    fun random() = items[Random().nextInt(itemCount)]

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.item_media_list

        @DimenRes
        private const val ICON_WIDTH_ID = R.dimen.music_list_icon_width

    }
}