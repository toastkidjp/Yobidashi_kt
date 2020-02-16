/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

/**
 * TODO extract last delimiter.
 * @author toastkidjp
 */
class ParentExtractor {

    operator fun invoke(path: String?): String? =
            when {
                path.isNullOrBlank() || !path.contains("/") -> path
                else -> path.substring(0, path.lastIndexOf("/"))
            }
}