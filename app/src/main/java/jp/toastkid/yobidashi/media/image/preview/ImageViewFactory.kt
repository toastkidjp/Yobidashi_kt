/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class ImageViewFactory {

    operator fun invoke(context: Context): ImageView {
        val view = PhotoView(context)
        view.layoutParams = layoutParams
        val horizontalMargin =
                context.resources.getDimensionPixelSize(R.dimen.image_viewer_item_horizontal_margin)
        view.setPadding(horizontalMargin, 0, horizontalMargin, 0)
        view.maximumScale = 15f

        return view
    }

    companion object {
        private val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

}