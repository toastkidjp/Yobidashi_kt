/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.search.history.SearchHistoryInsertion
import org.junit.After
import org.junit.Before
import org.junit.Test

class SearchActionTest {

    private lateinit var searchAction: SearchAction

    @MockK
    private lateinit var activityContext: Context

    private var category: String = "test"

    private var query: String = "test"

    private var currentUrl: String = "https://search.yahoo.co.jp"

    private var onBackground: Boolean = false

    private var saveHistory: Boolean = true

    @MockK
    private lateinit var viewModelSupplier: (Context) -> BrowserViewModel?

    @MockK
    private lateinit var preferenceApplierSupplier: (Context) -> PreferenceApplier

    @MockK
    private lateinit var urlFactory: UrlFactory

    @MockK
    private lateinit var browserViewModel: BrowserViewModel

    @MockK
    private lateinit var searchHistoryInsertion: SearchHistoryInsertion

    @SpyK(recordPrivateCalls = true)
    private var preferenceApplier: PreferenceApplier = mockk()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { preferenceApplier getProperty "isEnableSearchHistory"  }.returns(true)
        every { preferenceApplierSupplier.invoke(any()) }.returns(preferenceApplier)
        every { viewModelSupplier.invoke(any()) }.returns(browserViewModel)
        every { urlFactory.invoke(any(), any(), any()) }.returns(mockk())
        every { activityContext.getString(any(), any()) }.returns("test")
        every { browserViewModel.openBackground(any(), any()) }.just(Runs)
        every { browserViewModel.open(any()) }.just(Runs)

        mockkObject(Urls)
        every { Urls.isValidUrl(any()) }.returns(false)

        mockkObject(SearchHistoryInsertion)
        every { SearchHistoryInsertion.make(any(), any(), any()) }.returns(searchHistoryInsertion)
        every { searchHistoryInsertion.insert() }.returns(mockk())

        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())

        searchAction = SearchAction(
            activityContext,
            category,
            query,
            currentUrl,
            onBackground,
            saveHistory,
            viewModelSupplier,
            preferenceApplierSupplier,
            urlFactory
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testOpenSearchByNewTabOnForeground() {
        searchAction.invoke()

        verify(exactly = 1) { browserViewModel.open(any()) }
        verify(exactly = 0) { browserViewModel.openBackground(any(), any()) }
        verify(exactly = 1) { urlFactory.invoke(any(), any(), any()) }
        verify(exactly = 1) { searchHistoryInsertion.insert() }
    }

    @Test
    fun testOpenSearchOnBackground() {
        searchAction = SearchAction(
            activityContext,
            category,
            query,
            currentUrl,
            true,
            saveHistory,
            viewModelSupplier,
            preferenceApplierSupplier,
            urlFactory
        )

        searchAction.invoke()

        verify(exactly = 0) { browserViewModel.open(any()) }
        verify(exactly = 1) { browserViewModel.openBackground(any(), any()) }
        verify(exactly = 1) { urlFactory.invoke(any(), any(), any()) }
        verify(exactly = 1) { searchHistoryInsertion.insert() }
    }

    @Test
    fun testOpenUrlOnForeground() {
        searchAction = SearchAction(
            activityContext,
            category,
            query,
            currentUrl,
            onBackground,
            false,
            viewModelSupplier,
            preferenceApplierSupplier,
            urlFactory
        )

        searchAction.invoke()

        verify(exactly = 1) { browserViewModel.open(any()) }
        verify(exactly = 1) { urlFactory.invoke(any(), any(), any()) }
        verify(exactly = 0) { searchHistoryInsertion.insert() }
    }

}