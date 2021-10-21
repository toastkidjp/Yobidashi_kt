/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.usecase

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.databinding.FragmentSearchBinding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import org.junit.After
import org.junit.Before
import org.junit.Test

class ContentSwitcherUseCaseTest {

    @InjectMockKs
    private lateinit var contentSwitcherUseCase: ContentSwitcherUseCase

    @MockK
    private lateinit var binding: FragmentSearchBinding

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var setActionButtonState: (Boolean) -> Unit

    private val currentTitle: String = "Yahoo! JAPAN"

    private val currentUrl: String = "https://www.yahoo.co.jp"

    @MockK
    private lateinit var channel: Channel<String>

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { channel.send(any()) }.just(Runs)
        every { channel.close() }.returns(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testDispose() {
        contentSwitcherUseCase.dispose()

        verify(exactly = 1) { channel.close() }
    }

    @Test
    fun send() {
        contentSwitcherUseCase.send("test")

        coVerify(exactly = 1) { channel.send(any()) }
    }

}