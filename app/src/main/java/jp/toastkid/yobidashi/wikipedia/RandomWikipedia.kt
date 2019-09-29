/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.wikipedia

import kotlin.random.Random

/**
 * @author toastkidjp
 */
class RandomWikipedia {

    private val wikipediaApi = WikipediaApi()

    fun fetch(): String {
        val titles = wikipediaApi.invoke()?.filter { it.ns == 0 } ?: return ""
        return titles[Random.nextInt(titles.size)].title
    }
}