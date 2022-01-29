/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.suggestion

import okio.buffer
import okio.source
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Test cases of [SuggestionParser].
 *
 * @author toastkidjp
 */
class SuggestionParserTest {

    /**
     * Check of [SuggestionParser.parse].
     */
    @Test
    fun test_parse() {
        val file = File(javaClass.classLoader?.getResource(PATH_TO_RESOURCE)?.toURI())
        val xml = file.source().use { source ->
            source.buffer().use {
                it.readUtf8()
            }
        }
        val suggestions = SuggestionParser()(xml)
        assertEquals(10, suggestions.size)
    }

    companion object {
        private const val PATH_TO_RESOURCE = "suggestion/google.xml"
    }
}