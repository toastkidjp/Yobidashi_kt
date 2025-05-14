/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.preview.attach

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import jp.toastkid.lib.intent.BitmapShareIntentFactory

/**
 * @author toastkidjp
 */
class AttachToAnyAppUseCase(
    private val activityStarter: (Intent) -> Unit,
    private val intentFactory: BitmapShareIntentFactory = BitmapShareIntentFactory()
) {

    operator fun invoke(context: Context, bitmap: Bitmap) {
        val intent = intentFactory(context, bitmap)

        activityStarter(Intent.createChooser(intent, CHOOSER_TITLE))
    }

    companion object {
        private const val CHOOSER_TITLE = "Set as:"
    }

}
