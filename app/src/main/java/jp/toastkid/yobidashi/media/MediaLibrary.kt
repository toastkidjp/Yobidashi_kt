/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.support.v4.media.MediaBrowserCompat
import timber.log.Timber

/**
 * @author toastkidjp
 */
object MediaLibrary {

    private var current: MediaBrowserCompat.MediaItem? = null

    fun setCurrent(mediaItem: MediaBrowserCompat.MediaItem) {
        current = mediaItem
    }

    fun getMetadata(): MediaBrowserCompat.MediaItem? {
        return current
    }

    private fun loadArtwork(retriever: MediaMetadataRetriever): Bitmap? =
            try {
                val artwork = retriever.embeddedPicture
                BitmapFactory.decodeByteArray(artwork, 0, artwork.size)
            } catch (e: Exception) {
                Timber.w(e)
                null
            }
}