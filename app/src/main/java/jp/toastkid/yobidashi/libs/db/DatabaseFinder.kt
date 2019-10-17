/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.libs.db

import android.content.Context
import androidx.room.Room

/**
 * @author toastkidjp
 */
class DatabaseFinder {

    fun invoke(context: Context): AppDatabase {
        return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "yobidashi.db"
        )
                .fallbackToDestructiveMigration()
                .build()
    }
}