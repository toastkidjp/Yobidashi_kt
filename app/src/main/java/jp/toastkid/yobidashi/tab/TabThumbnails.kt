/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.tab

import android.content.Context
import jp.toastkid.lib.storage.FilesDir

/**
 * @author toastkidjp
 */
class TabThumbnails(contextSupplier: () -> Context) {

    private val folder = FilesDir(contextSupplier(), SCREENSHOT_DIR_PATH)

    fun assignNewFile(name: String) = folder.assignNewFile(name)

    fun clean() = folder.clean()

    fun delete(thumbnailName: String?) {
        if (thumbnailName.isNullOrBlank()) {
            return
        }

        val lastScreenshot = assignNewFile(thumbnailName)
        if (lastScreenshot.exists()) {
            lastScreenshot.delete()
        }
    }

    fun deleteUnused(exceptionalTabIds: Collection<String>) {
        folder.listFiles()
                .filter { !exceptionalTabIds.contains(it.name) }
                .forEach { it.delete() }
    }

    companion object {

        /**
         * Directory path to screenshot.
         */
        private const val SCREENSHOT_DIR_PATH: String = "tabs/screenshots"

    }
}