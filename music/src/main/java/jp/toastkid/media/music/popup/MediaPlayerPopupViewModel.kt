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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class MediaPlayerPopupViewModel : ViewModel() {

    private val _musics = mutableStateListOf<MediaBrowserCompat.MediaItem>()

    val musics: SnapshotStateList<MediaBrowserCompat.MediaItem> = _musics

    fun nextMusics(musics: List<MediaBrowserCompat.MediaItem>) {
        _musics.clear()
        _musics.addAll(musics)
    }

    var playing by mutableStateOf(false)

}