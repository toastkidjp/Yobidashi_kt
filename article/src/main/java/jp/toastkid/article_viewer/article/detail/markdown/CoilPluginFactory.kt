/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail.markdown

import android.content.Context
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.coil.CoilImagesPlugin

/**
 * @author toastkidjp
 */
class CoilPluginFactory {

    operator fun invoke(context: Context): CoilImagesPlugin {
        val imageLoader = ImageLoader.Builder(context).build()
        return CoilImagesPlugin.create(
                object : CoilImagesPlugin.CoilStore {
                    override fun load(drawable: AsyncDrawable) =
                            ImageRequest.Builder(context)
                                    .defaults(imageLoader.defaults)
                                    .data(drawable.destination)
                                    .crossfade(true)
                                    .transformations(CircleCropTransformation())
                                    .build()

                    override fun cancel(disposable: Disposable) =
                            disposable.dispose()
                },
                imageLoader
        )
    }

}