/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.article_viewer.article.list.paging

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import jp.toastkid.article_viewer.article.list.SearchResult
import org.junit.Before
import org.junit.Test

class SimpleComparatorTest {

    private lateinit var simpleComparator: SimpleComparator

    @RelaxedMockK
    private lateinit var searchItem0: SearchResult

    @Before
    fun setUp() {
        simpleComparator = SimpleComparator()

        MockKAnnotations.init(this)
    }

    @Test
    fun testAreItemsTheSame() {
        simpleComparator.areItemsTheSame(searchItem0, searchItem0)
    }

    @Test
    fun testAreContentsTheSame() {
        simpleComparator.areContentsTheSame(searchItem0, searchItem0)
    }

}