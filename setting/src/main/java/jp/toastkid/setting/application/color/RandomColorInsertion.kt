/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.setting.application.color

import android.graphics.Color
import jp.toastkid.yobidashi.settings.color.SavedColor
import jp.toastkid.yobidashi.settings.color.SavedColorRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Random

/**
 * @author toastkidjp
 */
class RandomColorInsertion(private val repository: SavedColorRepository) {

    private val random = Random()

    /**
     * Insert random colors.
     *
     * @param context
     */
    operator fun invoke(afterInserted: () -> Unit, ioDispatcher: CoroutineDispatcher = Dispatchers.IO): Job {
        val bg = makeRandomColor()

        val font = makeRandomColor()

        return CoroutineScope(ioDispatcher).launch {
            repository.add(SavedColor.make(bg, font))
            afterInserted()
        }
    }

    private fun makeRandomColor(): Int {
        return Color.argb(
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX)
        )
    }

    companion object {
        private const val COLOR_CODE_MAX = 255
    }
}