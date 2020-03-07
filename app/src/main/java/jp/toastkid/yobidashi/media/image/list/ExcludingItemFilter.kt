/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.list

/**
 * @author toastkidjp
 */
class ExcludingItemFilter(private val excludingItems: Set<String>) {

    operator fun invoke(path: String?) =
            excludingItems.isNullOrEmpty() || !excludingItems.contains(path)
}