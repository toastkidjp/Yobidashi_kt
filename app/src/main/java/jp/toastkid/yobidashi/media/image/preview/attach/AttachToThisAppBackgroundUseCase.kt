/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview.attach

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.lib.window.WindowRectCalculatorCompat
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.settings.background.load.ImageStoreService
import jp.toastkid.yobidashi.settings.fragment.DisplayingSettingFragment

/**
 * @author toastkidjp
 */
class AttachToThisAppBackgroundUseCase(
    private val contentViewModel: ContentViewModel,
    private val imageStoreServiceFactory: (Context) -> ImageStoreService = {
        ImageStoreService(
            FilesDir(it, DisplayingSettingFragment.getBackgroundDirectory()),
            PreferenceApplier(it)
        )
    },
    private val windowRectCalculatorCompat: WindowRectCalculatorCompat = WindowRectCalculatorCompat()
) {

    operator fun invoke(context: Context, uri: Uri, image: Bitmap) {
        val displaySize = windowRectCalculatorCompat.invoke(context as? Activity) ?: return
        imageStoreServiceFactory(context)(image, uri, displaySize)
        contentViewModel.refresh()
        contentViewModel.snackShort(R.string.done_addition)
    }

}