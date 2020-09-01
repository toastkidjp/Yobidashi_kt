/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview.attach

import android.graphics.Bitmap
import android.net.Uri
import android.view.View

/**
 * @author toastkidjp
 */
class MenuActionUseCase(
        private val attachToThisAppBackgroundUseCase: AttachToThisAppBackgroundUseCase,
        private val attachToAnyAppUseCase: AttachToAnyAppUseCase,
        private val uriSupplier: () -> Uri?,
        private val bitmapSupplier: () -> Bitmap?
) {

    fun thisApp(v: View) {
        val uri = uriSupplier() ?: return
        val image = bitmapSupplier() ?: return
        attachToThisAppBackgroundUseCase.invoke(v.context, uri, image)
    }

    fun otherApp(v: View) {
        val image = bitmapSupplier() ?: return
        attachToAnyAppUseCase.invoke(v.context, image)
    }

}