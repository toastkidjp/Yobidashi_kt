/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi4.markdown.domain.model

import jp.toastkid.markdown.domain.model.InternalLinkScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class InternalLinkSchemeTest {

    private lateinit var internalLinkScheme: InternalLinkScheme

    @Before
    fun setUp() {
        internalLinkScheme = InternalLinkScheme()
    }

    @Test
    fun makeLink() {
        assertEquals(
                "[tomato](http://internal/tomato)",
                internalLinkScheme.makeLink("tomato")
        )
        assertEquals(
            "[tomato stake](http://internal/tomato%20stake)",
            internalLinkScheme.makeLink("tomato stake")
        )
    }

    @Test
    fun isInternalLink() {
        assertFalse(internalLinkScheme.isInternalLink(""))
        assertFalse(internalLinkScheme.isInternalLink(" "))
        assertFalse(internalLinkScheme.isInternalLink("https://www.yahoo.co.jp"))
        assertFalse(internalLinkScheme.isInternalLink("tomato"))
        assertTrue(internalLinkScheme.isInternalLink("http://internal/tomato"))
    }

    @Test
    fun extract() {
        assertEquals(
                "",
                internalLinkScheme.extract("")
        )
        assertEquals(
                " ",
                internalLinkScheme.extract(" ")
        )
        assertEquals(
                "tomato",
                internalLinkScheme.extract("tomato")
        )
        assertEquals(
                "https://www.yahoo.co.jp",
                internalLinkScheme.extract("https://www.yahoo.co.jp")
        )
        assertEquals(
                "tomato",
                internalLinkScheme.extract("http://internal/tomato")
        )
    }
}