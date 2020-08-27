/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * @author toastkidjp
 */
class EditorChooserInvokingUseCase(
        private val pathFinder: () -> String?,
        private val showErrorMessage: () -> Unit,
        private val activityStarter: (Intent) -> Unit
) {
    private val imageEditChooserFactory = ImageEditChooserFactory()

    fun invoke(context: Context) {
        val path = pathFinder()
        if (path == null) {
            showErrorMessage()
            return
        }

        try {
            activityStarter(imageEditChooserFactory(context, path))
        } catch (e: ActivityNotFoundException) {
            Timber.w(e)
            showErrorMessage()
        }
    }

}