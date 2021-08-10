/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.rss.extractor

import android.graphics.Color
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.Toaster
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test

class RssUrlFinderTest {

    @InjectMockKs
    private lateinit var rssUrlFinder: RssUrlFinder

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var urlValidator: RssUrlValidator

    @MockK
    private lateinit var rssUrlExtractor: RssUrlExtractor

    @MockK
    private lateinit var htmlApi: HtmlApi

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @SpyK(recordPrivateCalls = true)
    private var response: Response = mockk()

    @MockK
    private lateinit var body: ResponseBody

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { preferenceApplier.colorPair() }.returns(ColorPair(Color.BLACK, Color.WHITE))
        every { preferenceApplier.saveNewRssReaderTargets(any()) }.just(Runs)
        every { urlValidator.invoke(any()) }.returns(false)
        every { htmlApi.invoke(any()) }.returns(response)
        every { rssUrlExtractor.invoke(any()) }.returns(listOf("https://rss.yahoo.co.jp/1"))
        every { response getProperty "isSuccessful" }.returns(true)
        every { response getProperty "body" }.returns(body)
        every { body.string() }.returns("test")

        mockkObject(Toaster)
        every { Toaster.snackShort(any(), any<Int>(), any()) }.returns(mockk())
        every { Toaster.snackShort(any(), any<String>(), any()) }.returns(mockk())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        rssUrlFinder.invoke("https://www.yahoo.co.jp", { mockk() })
    }

}