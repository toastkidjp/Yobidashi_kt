/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.gesture

import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.BitmapCompressor
import jp.toastkid.yobidashi.libs.ThumbnailGenerator
import jp.toastkid.lib.storage.ExternalFileAssignment
import jp.toastkid.lib.ContentViewModel

/**
 * @author toastkidjp
 */
class CanvasStoreScenario {

    private val thumbnailGenerator = ThumbnailGenerator()

    private val externalFileAssignment = ExternalFileAssignment()

    private val bitmapCompressor = BitmapCompressor()

    operator fun invoke(activity: FragmentActivity, canvasView: View) {
        val bitmap = thumbnailGenerator.invoke(canvasView) ?: return

        val file = externalFileAssignment(
                activity,
                "GestureMemo_${System.currentTimeMillis()}.png"
        )

        bitmapCompressor.invoke(bitmap, file)

        ViewModelProvider(activity).get(ContentViewModel::class.java)
                .snackShort(activity.getString(MESSAGE_ID, file.absolutePath))
    }

    companion object {

        @StringRes
        private val MESSAGE_ID = R.string.message_store_gesture_memo

    }
}