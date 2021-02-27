/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.favorite

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteSearchTest {

    @Test
    fun testWith() {
        val favoriteSearch = FavoriteSearch.with("category", "query")

        assertEquals(0, favoriteSearch.id)
        assertEquals("category", favoriteSearch.category)
        assertEquals("query", favoriteSearch.query)
    }

}