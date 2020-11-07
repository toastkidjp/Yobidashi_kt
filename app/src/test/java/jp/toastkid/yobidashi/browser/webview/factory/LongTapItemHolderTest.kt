/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.factory

import android.os.Bundle
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class LongTapItemHolderTest {

    private lateinit var longTapItemHolder: LongTapItemHolder

    @Before
    fun setUp() {
        longTapItemHolder = LongTapItemHolder()
    }

    @Test
    fun testReset() {
        longTapItemHolder.title = "title"
        longTapItemHolder.anchor = "https://test.com"

        longTapItemHolder.reset()

        assertTrue(longTapItemHolder.title.isEmpty())
        assertTrue(longTapItemHolder.anchor.isEmpty())
    }

    @Test
    fun testExtract() {
        val bundle = mockk<Bundle>()
        every { bundle.get("title") }.returns("test_title")
        every { bundle.get("url") }.returns("https://test.com")

        longTapItemHolder.extract(bundle)

        assertEquals("test_title", longTapItemHolder.title)
        assertEquals("https://test.com", longTapItemHolder.anchor)
    }

    @Test
    fun testExtractTrim() {
        val bundle = mockk<Bundle>()
        every { bundle.get("title") }.returns("  test_title  ")
        every { bundle.get("url") }.returns("https://test.com")

        longTapItemHolder.extract(bundle)

        assertEquals("test_title", longTapItemHolder.title)
        assertEquals("https://test.com", longTapItemHolder.anchor)
    }
}