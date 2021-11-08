/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.Urls
import jp.toastkid.search.SearchQueryExtractor
import jp.toastkid.yobidashi.search.SearchFragment
import org.junit.After
import org.junit.Before
import org.junit.Test

class SearchFragmentFactoryUseCaseTest {
    
    private lateinit var searchFragmentFactoryUseCase: SearchFragmentFactoryUseCase

    @Before
    fun setUp() {
        searchFragmentFactoryUseCase = SearchFragmentFactoryUseCase()
        
        mockkConstructor(SearchQueryExtractor::class)
        every { anyConstructed<SearchQueryExtractor>().invoke(any<String>()) }.returns("test")
        
        mockkObject(Urls)
        every { Urls.isValidUrl(any()) }.returns(true)
        
        mockkObject(SearchFragment)
        every { SearchFragment.makeWith(any(), any()) }.returns(mockk())
        every { SearchFragment.makeWithQuery(any(), any(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNormalCase() {
        searchFragmentFactoryUseCase.invoke("title" to "https://www.yahoo.co.jp")

        verify(exactly = 1) { anyConstructed<SearchQueryExtractor>().invoke(any<String>()) }
        verify(exactly = 1) { SearchFragment.makeWith(any(), any()) }
        verify(exactly = 0) { SearchFragment.makeWithQuery(any(), any(), any()) }
    }

}