/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.color

import android.content.Context
import android.graphics.Color
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import timber.log.Timber
import java.util.*

/**
 * @author toastkidjp
 */
class RandomColorInsertion {

    private val random = Random()

    /**
     * Insert random colors.
     *
     * @param context
     */
    operator fun invoke(context: Context): Disposable {
        val bg = Color.argb(
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX)
        )

        val font = Color.argb(
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX)
        )

        return Completable.fromAction {
            DatabaseFinder().invoke(context)
                    .savedColorRepository()
                    .add(SavedColor.make(bg, font))
        }
                .subscribeOn(Schedulers.io())
                .subscribe({}, Timber::e)
    }

    companion object {
        private const val COLOR_CODE_MAX = 255
    }
}