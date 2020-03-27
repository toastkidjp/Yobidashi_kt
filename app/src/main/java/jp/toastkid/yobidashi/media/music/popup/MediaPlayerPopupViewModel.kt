/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.music.popup

import android.support.v4.media.MediaBrowserCompat
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
}