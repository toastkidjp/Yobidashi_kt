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
    companion object {
        fun makeBucket(bucketName: String) = Image("", bucketName, true)
    }
}