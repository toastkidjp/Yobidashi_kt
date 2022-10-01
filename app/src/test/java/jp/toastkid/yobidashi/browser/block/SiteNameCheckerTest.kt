/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.block

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.unmockkAll
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class SiteNameCheckerTest {

    @InjectMockKs
    private lateinit var siteNameChecker: SiteNameChecker

    @org.junit.Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @org.junit.After
    fun tearDown() {
        unmockkAll()
    }

    @org.junit.Test
    fun invoke() {
        assertFalse(siteNameChecker("www.yahoo.co.jp"))
        assertFalse(siteNameChecker("xn--2ch-2d8eo32c60z.xyz"))
        assertTrue(siteNameChecker("dangerous.xyz"))
        assertTrue(siteNameChecker("dangerous.jp.net"))
        assertTrue(siteNameChecker("rt.gsspat.jp"))
        assertTrue(siteNameChecker("webnew.net"))
        assertTrue(siteNameChecker("jp.img4.uk"))
    }
}