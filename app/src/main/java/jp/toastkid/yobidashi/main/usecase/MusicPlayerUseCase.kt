/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.usecase

import android.view.View
import androidx.activity.result.ActivityResultLauncher
import jp.toastkid.media.music.popup.MediaPlayerPopup

class MusicPlayerUseCase(
    private val mediaPermissionRequestLauncher: ActivityResultLauncher<((Boolean) -> Unit)?>,
    private var mediaPlayerPopup: MediaPlayerPopup? = null
) {

    operator fun invoke(parent: View?) {
        if (parent == null) {
            return
        }

        if (mediaPlayerPopup == null) {
            mediaPlayerPopup = MediaPlayerPopup(parent.context)
        }

        mediaPermissionRequestLauncher.launch {
            if (it.not()) {
                return@launch
            }

            mediaPlayerPopup?.show(parent)
        }
    }
}