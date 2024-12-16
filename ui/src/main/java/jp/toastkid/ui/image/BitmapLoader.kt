/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap

class BitmapLoader {

    suspend operator fun invoke(context: Context, uri: Uri): Bitmap? =
        context.imageLoader
            .execute(ImageRequest.Builder(context).data(uri).build())
            .image
            ?.toBitmap()

}
