/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.image.factory

import android.content.Context
import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.gif.GifDecoder

class GifImageLoaderFactory {

    operator fun invoke(context: Context) = ImageLoader.Builder(context)
        .components(
            ComponentRegistry.Builder()
                .add(GifDecoder.Factory())
                .build()
        )
        .build()

}