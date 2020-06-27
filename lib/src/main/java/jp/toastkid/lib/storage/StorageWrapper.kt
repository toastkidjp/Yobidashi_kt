/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.storage

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * @author toastkidjp
 */
interface StorageWrapper {

    /**
     * Subclass should be override this method.
     *
     * @param context [Context]
     */
    fun getDir(context: Context): File

    /**
     * Get file object.
     * @param index
     *
     * @return File object
     */
    operator fun get(index: Int): File?

    /**
     * Remove item which specified position.
     *
     * @param index
     */
    fun removeAt(index: Int): Boolean

    fun findByName(name: String): File?

    fun delete(name: String)

    /**
     * Delete all files.
     */
    fun clean()

    /**
     * Assign new file.
     * @param uri
     *
     * @return [File]
     */
    fun assignNewFile(uri: Uri): File

    /**
     * Assign new file.
     * @param name
     *
     * @return [File]
     */
    fun assignNewFile(name: String): File

    /**
     * Internal method.
     * @return
     */
    fun listFiles(): Array<File>
}