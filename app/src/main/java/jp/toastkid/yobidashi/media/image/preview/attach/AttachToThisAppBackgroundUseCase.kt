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
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.settings.background.load.ImageStoreService
import jp.toastkid.yobidashi.settings.fragment.DisplayingSettingFragment

/**
 * @author toastkidjp
 */
class AttachToThisAppBackgroundUseCase(private val contentViewModel: ContentViewModel) {

    operator fun invoke(context: Context, uri: Uri, image: Bitmap) {
        ImageStoreService(
                FilesDir(context, DisplayingSettingFragment.getBackgroundDirectory()),
                PreferenceApplier(context)
        )(image, uri, (context as? Activity)?.windowManager?.defaultDisplay)
        contentViewModel.snackShort(R.string.done_addition)
    }

}