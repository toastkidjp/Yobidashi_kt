/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.factory

import android.content.Context
import android.os.Build
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

class GifImageLoaderFactory {

    operator fun invoke(context: Context) = ImageLoader.Builder(context)
        .components(
            ComponentRegistry.Builder()
                .add(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ImageDecoderDecoder.Factory()
                    else GifDecoder.Factory()
                )
                .build()
        )
        .build()

}