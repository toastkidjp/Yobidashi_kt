/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

/**
 * @author toastkidjp
 */
data class Image(val path: String, val name: String, var itemCount: Int = 0, val isBucket: Boolean = false) {

    fun makeExcludingId() = if (isBucket) parentExtractor(path) else path

    fun makeDisplayName(): String = if (isBucket) "$name / $itemCount images" else name

    companion object {
        private val parentExtractor = ParentExtractor()

        fun makeBucket(bucketName: String, path: String, itemCount: Int) =
                Image(path, bucketName, 0, true)
    }
}