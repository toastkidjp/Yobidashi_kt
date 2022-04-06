/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.media.music.popup

import android.support.v4.media.MediaBrowserCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class MediaPlayerPopupViewModel : ViewModel() {

    private val _clickItem = MutableLiveData<MediaBrowserCompat.MediaItem>()

    val clickItem: LiveData<MediaBrowserCompat.MediaItem> = _clickItem

    fun clickItem(item: MediaBrowserCompat.MediaItem) {
        _clickItem.postValue(item)
    }

    private val _lyrics = MutableLiveData<String>()

    val clickLyrics: LiveData<String> = _lyrics

    fun clickLyrics(name: CharSequence?) {
        name?.also { _lyrics.postValue(it.toString()) }
    }

    private val _musics = MutableLiveData<List<MediaBrowserCompat.MediaItem>>()

    val musics: LiveData<List<MediaBrowserCompat.MediaItem>> = _musics

    fun nextMusics(musics: List<MediaBrowserCompat.MediaItem>) {
        _musics.postValue(musics)
    }

    var playing by mutableStateOf(false)

}