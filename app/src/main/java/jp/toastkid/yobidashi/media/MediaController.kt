/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

/**
 * @author toastkidjp
 */
class MediaController {
    private val mediaPlayer = MediaPlayer();

    fun playNew(context: Context, path: Uri?) {
        if (path == null) {
            return
        }

        mediaPlayer.reset()
        mediaPlayer.setDataSource(context, path)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    /**
     *
     * @return displayStop
     */
    fun switch(): Boolean =
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                false
            } else {
                mediaPlayer.start()
                true
            }

    fun reset() {
        mediaPlayer.reset()
    }

    fun dispose() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}