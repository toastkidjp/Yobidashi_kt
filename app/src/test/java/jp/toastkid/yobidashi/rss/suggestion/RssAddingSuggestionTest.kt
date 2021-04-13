/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.rss.suggestion

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.rss.extractor.RssUrlValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class RssAddingSuggestionTest {

    @InjectMockKs
    private lateinit var rssAddingSuggestion: RssAddingSuggestion

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var rssUrlValidator: RssUrlValidator

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(Toaster)
        every { Toaster.snackLong(any(), any<Int>(), any(), any(), any()) }.answers { mockk() }
        every { preferenceApplier.colorPair() }.returns(mockk())

        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        unmockkAll()

        Dispatchers.resetMain()
    }

    @Test
    fun invoke() {
        every { rssUrlValidator.invoke(any()) }.returns(true)
        every { preferenceApplier.containsRssTarget(any()) }.returns(false)

        rssAddingSuggestion.invoke(mockk(), "https://www.yahoo.co.jp")

        verify(exactly = 1) { rssUrlValidator.invoke(any()) }
        verify(exactly = 1) { preferenceApplier.containsRssTarget(any()) }
        verify(exactly = 1) { Toaster.snackLong(any(), any<Int>(), any(), any(), any()) }
        verify(exactly = 1) { preferenceApplier.colorPair() }
    }

}