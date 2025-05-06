/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.media.music.permission

import android.Manifest
import android.os.Build

class MusicPlayerPermissions(private val sdkInt: Int = Build.VERSION.SDK_INT) {

    operator fun invoke() =
        if (sdkInt >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        else
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )

}