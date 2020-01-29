/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media

import android.content.ContentResolver
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri

/**
 * @author toastkidjp
 */
class MusicFileFinder(private val contentResolver: ContentResolver) {

    private val albumArtFinder = AlbumArtFinder(contentResolver)

    operator fun invoke(): MutableList<MediaMetadataCompat> {
        val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                SORT_ORDER
        )

        val result = mutableListOf<MediaMetadataCompat>()

        while (cursor?.moveToNext() == true) {
            val meta = MediaMetadataCompat.Builder()
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)).toString()
                    )
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_TITLE,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    )
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_ARTIST,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    )
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_ALBUM,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    )
                    /*.putString(
                            MediaMetadataCompat.METADATA_KEY_DATE,
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)).toString()
                    )*/
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                            "content://media/external/audio/albumart".toUri().buildUpon().appendEncodedPath(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()).build().toString()
                    )
                    .putString(
                            MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    )
                    .putBitmap(
                            MediaMetadataCompat.METADATA_KEY_ART,
                            albumArtFinder(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))
                    )
                    .build()

            result.add(meta)
        }

        cursor?.close()
        return result
    }

    companion object {
        private const val SORT_ORDER = "${MediaStore.Audio.AudioColumns.ALBUM} ASC"
    }
}