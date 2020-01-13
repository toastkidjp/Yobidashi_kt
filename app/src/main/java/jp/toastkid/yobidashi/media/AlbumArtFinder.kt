/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import java.io.FileNotFoundException

/**
 * @author toastkidjp
 */
class AlbumArtFinder(private val contentResolver: ContentResolver) {

    operator fun invoke(id: Long): Bitmap? {
        val album1Uri = ContentUris.withAppendedId(albumArtUri, id)
        try {
            return BitmapFactory.decodeStream(contentResolver.openInputStream(album1Uri))
        } catch (err: FileNotFoundException) {
            err.printStackTrace()
        }
        return null
    }

    companion object {
        private val albumArtUri = "content://media/external/audio/albumart".toUri()
    }
}