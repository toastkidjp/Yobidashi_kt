/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.url_suggestion

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import jp.toastkid.lib.Urls
import jp.toastkid.yobidashi.browser.UrlItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class RtsSuggestionUseCaseTest {

    @InjectMockKs
    private lateinit var rtsSuggestionUseCase: RtsSuggestionUseCase

    @Suppress("unused")
    private val dispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @MockK
    private lateinit var uri: Uri

    @MockK
    private lateinit var itemCallback: (UrlItem) -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(Urls)
        coEvery { Urls.isInvalidUrl(any()) }.returns(false)

        mockkStatic(Uri::class)
        coEvery { Uri.parse(any()) }.returns(uri)

        coEvery { uri.host }.returns("www.twitter.com")
        coEvery { uri.pathSegments }.returns(listOf("test"))
        coEvery { itemCallback.invoke(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        runBlocking {
            rtsSuggestionUseCase.invoke("https://www.twitter.com/test", itemCallback)
            coVerify { itemCallback.invoke(any()) }
        }
    }

    @Test
    fun testInvalidUriCase() {
        coEvery { Urls.isInvalidUrl(any()) }.returns(true)

        runBlocking {
            rtsSuggestionUseCase.invoke("https://www.twitter.com/test", itemCallback)
            coVerify(inverse = true) { itemCallback.invoke(any()) }
        }
    }

    @Test
    fun testNotTargetUriHostCase() {
        coEvery { uri.host }.returns("www.yahoo.com")

        runBlocking {
            rtsSuggestionUseCase.invoke("https://www.twitter.com/test", itemCallback)
            coVerify(inverse = true) { itemCallback.invoke(any()) }
        }
    }

    @Test
    fun testCannotExtractPathsCase() {
        coEvery { uri.pathSegments }.returns(listOf(""))

        runBlocking {
            rtsSuggestionUseCase.invoke("https://www.twitter.com/test", itemCallback)
            coVerify(inverse = true) { itemCallback.invoke(any()) }
        }
    }

}