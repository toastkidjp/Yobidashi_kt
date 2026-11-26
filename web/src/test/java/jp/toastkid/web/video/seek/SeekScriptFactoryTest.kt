/*
 * Copyright (c) 2026 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.video.seek

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SeekScriptFactoryTest {

    private lateinit var subject: SeekScriptFactory

    @Before
    fun setUp() {
        subject = SeekScriptFactory()
    }

    @Test
    fun makeScript() {
        val script = subject.makeBackwardSeekScript(1)

        assertTrue(script.contains("var targetTime = endTime - 1"))
    }

    @Test
    fun makeRelativeSeekScript() {
        val script = subject.makeRelativeSeekScript(10)

        assertTrue(script.contains("var targetTime = current + 10;"))
    }

    @Test
    fun isTarget() {
        assertFalse(subject.isTarget(null))
        assertFalse(subject.isTarget(""))
        assertFalse(subject.isTarget("https://www.yahoo.co.jp"))
        assertTrue(subject.isTarget("https://www.stockvoice.tv"))
    }

}