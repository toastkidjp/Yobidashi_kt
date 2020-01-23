/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.rss

/**
 * @author toastkidjp
 */
data class Rss(
        /** RSS' title.  */
        var title: String? = null,

        /** RSS' subjects.  */
        var subjects: MutableList<String> = mutableListOf(),

        /** RSS' creator.  */
        var creator: String? = null,

        /** RSS' date.  */
        var date: String? = null,

        /** RSS' items.  */
        var items: MutableList<Item> = mutableListOf(),

        /** RSS' Link.  */
        var link: String? = null,

        /** RSS' URL.  */
        var url: String? = null,

        /** RSS' description.  */
        var description: String? = null
) {

    companion object {

        /**
         * Empty object.
         */
        private val EMPTY = Rss()

        fun empty() = EMPTY
    }

}