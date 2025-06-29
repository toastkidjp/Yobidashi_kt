/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.api.rss.model

/**
 * @author toastkidjp
 */
data class Item(
        var title: String = "",
        var link: String = "",
        var date: String = "",
        var description: String = "",
        var source: String = "",
        var content: StringBuilder = StringBuilder()
) {

        private val key = title + link + source

        fun key() = key

}