/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.lib.model

data class LoadInformation(
    val tabId: String,
    val title: String,
    val url: String,
    private val lastUpdated: Long = System.currentTimeMillis()
) {

    fun expired() = System.currentTimeMillis() - lastUpdated > 3000L

}