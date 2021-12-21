/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.rss.suggestion

import androidx.lifecycle.ViewModelStoreOwner
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.rss.extractor.RssUrlValidator
import kotlinx.coroutines.Dispatchers
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

    @Suppress("unused")
    private val mainDispatcher = Dispatchers.Unconfined

    @Suppress("unused")
    private val backgroundDispatcher = Dispatchers.Unconfined

    @MockK
    private lateinit var contentViewModelFactory: (ViewModelStoreOwner) -> ContentViewModel?

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { contentViewModelFactory.invoke(any()) }.returns(contentViewModel)
        every { contentViewModel.snackWithAction(any(), any(), any()) }.returns(mockk())
        every { preferenceApplier.colorPair() }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        every { rssUrlValidator.invoke(any()) }.returns(true)
        every { preferenceApplier.containsRssTarget(any()) }.returns(false)

        rssAddingSuggestion.invoke(mockk(), "https://www.yahoo.co.jp")

        verify(exactly = 1) { rssUrlValidator.invoke(any()) }
        verify(exactly = 1) { preferenceApplier.containsRssTarget(any()) }
        verify(exactly = 1) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 1) { preferenceApplier.colorPair() }
    }

    @Test
    fun testValidatorReturnsFalseCase() {
        every { rssUrlValidator.invoke(any()) }.returns(false)
        every { preferenceApplier.containsRssTarget(any()) }.returns(false)

        rssAddingSuggestion.invoke(mockk(), "https://www.yahoo.co.jp")

        verify(exactly = 1) { rssUrlValidator.invoke(any()) }
        verify(exactly = 0) { preferenceApplier.containsRssTarget(any()) }
        verify(exactly = 0) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 0) { preferenceApplier.colorPair() }
    }

    @Test
    fun testContainsRssTargetReturnsTrueCase() {
        every { rssUrlValidator.invoke(any()) }.returns(true)
        every { preferenceApplier.containsRssTarget(any()) }.returns(true)

        rssAddingSuggestion.invoke(mockk(), "https://www.yahoo.co.jp")

        verify(exactly = 1) { rssUrlValidator.invoke(any()) }
        verify(exactly = 1) { preferenceApplier.containsRssTarget(any()) }
        verify(exactly = 0) { contentViewModel.snackWithAction(any(), any(), any()) }
        verify(exactly = 0) { preferenceApplier.colorPair() }
    }

}