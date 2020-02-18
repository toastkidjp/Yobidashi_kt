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
data class Image(val path: String, val name: String, val isBucket: Boolean) {

    fun makeExcludingId() = if (isBucket) parentExtractor(path) else path

    companion object {
        private val parentExtractor = ParentExtractor()

        fun makeBucket(bucketName: String, path: String) = Image(path, bucketName, true)
    }
}