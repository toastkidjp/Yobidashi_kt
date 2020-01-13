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

/**
 * @author toastkidjp
 */
class AudioFileFinder {

    operator fun invoke(contentResolver: ContentResolver?, itemConsumer: (Audio) -> Unit) {
        if (contentResolver == null) {
            return
        }
        val sortOrder = MediaStore.Audio.AudioColumns.ALBUM + " ASC"
        val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                sortOrder
        )

        while (cursor?.moveToNext() == true) {
            val audio = Audio(
                    id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                    title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                    artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                    album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    date = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)),
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            )
            itemConsumer(audio)
        }

        cursor?.close()
    }
}