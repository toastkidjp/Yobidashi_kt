/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.music

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import timber.log.Timber
import java.io.FileNotFoundException

/**
 * @author toastkidjp
 */
class AlbumArtFinder(private val contentResolver: ContentResolver) {

    operator fun invoke(album1Uri: Uri): Bitmap? {
        try {
            return BitmapFactory.decodeStream(contentResolver.openInputStream(album1Uri))
        } catch (e: FileNotFoundException) {
            Timber.e(e)
        }
        return null
    }

}